package com.ctrip.framework.cdashboard.hbase.plugin.filter;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Customization HBase Filter for CDashboard
 */
public class TagsRowFilter implements Filter {
    public TreeMap<Short, TreeSet<Integer>> tagsFilter;
    private byte[][] tagIdList;
    private int[][] valueIdsList;
    private byte[][] valueIdBytesList;

    private byte[] secondOffsets;
    private byte[][] seconds;
    private int columnFound = 0;
    private int maxColumnCnt = 0;

    public TagsRowFilter() {
        super();
    }

    public TagsRowFilter copy() {
        TagsRowFilter rt = new TagsRowFilter();
        rt.tagsFilter = this.tagsFilter;
        return rt;
    }

    public void setTagsFilterInfo(TreeMap<Short, TreeSet<Integer>> tagsFilter) {
        if (tagsFilter != null && tagsFilter.size() > 0) {
            this.tagsFilter = tagsFilter;
            Iterator<Entry<Short, TreeSet<Integer>>> it = this.tagsFilter.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Short, TreeSet<Integer>> entry = it.next();
                TreeSet<Integer> set = entry.getValue();
                set.remove(0);
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    it.remove();
                }
            }
        }
    }

    public void setSecondOffsets(int[] qualifiers) {
        if (qualifiers == null || qualifiers.length == 0) {
            secondOffsets = null;
            seconds = null;
            return;
        }
        TreeMap<Byte, TreeSet<Byte>> collector = new TreeMap<Byte, TreeSet<Byte>>();
        for (int qualifier : qualifiers) {
            byte offset = (byte) ((qualifier & 0xFF00) >> 8);
            byte second = (byte) (qualifier & 0xFF);
            TreeSet<Byte> secondSet = collector.get(offset);
            if (secondSet == null) {
                secondSet = new TreeSet<Byte>();
                collector.put(offset, secondSet);
            }
            secondSet.add(second);
        }
        secondOffsets = new byte[collector.size()];
        seconds = new byte[collector.size()][];
        int idx = 0;
        for (Entry<Byte, TreeSet<Byte>> entry : collector.entrySet()) {
            secondOffsets[idx] = entry.getKey();
            TreeSet<Byte> value = entry.getValue();
            seconds[idx] = new byte[value.size()];
            int subIdx = 0;
            for (Byte aValue : value) {
                seconds[idx][subIdx] = aValue;
                subIdx++;
            }
            idx++;
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (tagsFilter == null || tagsFilter.size() == 0) {
            out.writeShort(0);
        } else {
            out.writeShort(tagsFilter.size());
            for (Entry<Short, TreeSet<Integer>> entry : tagsFilter.entrySet()) {
                out.writeShort(entry.getKey());
                TreeSet<Integer> value = entry.getValue();
                if (value == null || value.size() == 0) {
                    out.writeInt(0);
                } else {
                    out.writeInt(value.size());
                    for (int val : value) {
                        out.writeInt(val);
                    }
                }
            }
        }
        if (secondOffsets == null || secondOffsets.length == 0) {
            out.writeShort(0);
        } else {
            out.writeShort(secondOffsets.length);
            for (byte offset : secondOffsets) {
                out.writeByte(offset);
            }
            for (byte[] subSeconds : seconds) {
                out.writeShort(subSeconds.length);
                for (byte second : subSeconds) {
                    out.writeByte(second);
                }
            }
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        short tagNum = in.readShort();
        if (tagNum > 0) {
            tagIdList = new byte[tagNum][];
            valueIdsList = new int[tagNum][];
            valueIdBytesList = new byte[tagNum][];
            int pos = 0;
            while (tagNum > 0) {
                short tagNameId = in.readShort(); //>0
                int[] tagValues = null;
                int valLen = in.readInt();
                if (valLen > 0) {
                    if (valLen < 5) {
                        valueIdBytesList[pos] = new byte[valLen << 2];
                        for (int idx = 0; idx < valLen; idx++) {
                            int valId = in.readInt();
                            valueIdBytesList[pos][(idx << 2)] = (byte) ((valId & 0xFF000000) >> 24);
                            valueIdBytesList[pos][(idx << 2) + 1] = (byte) ((valId & 0xFF0000) >> 16);
                            valueIdBytesList[pos][(idx << 2) + 2] = (byte) ((valId & 0xFF00) >> 8);
                            valueIdBytesList[pos][(idx << 2) + 3] = (byte) (valId & 0xFF);
                        }
                    } else {
                        tagValues = new int[valLen];
                        for (int i = 0; i < valLen; i++) {
                            tagValues[i] = in.readInt();
                        }
                    }
                }
                tagIdList[pos] = new byte[]{(byte) ((tagNameId & 0xFF00) >> 8), (byte) (tagNameId & 0xFF)};
                if (valLen >= 5) {
                    valueIdsList[pos] = tagValues;
                }
                pos++;
                tagNum--;
            }
        }
        short offsetNum = in.readShort();
        if (offsetNum > 0) {
            secondOffsets = new byte[offsetNum];
            for (int i = 0; i < offsetNum; i++) {
                secondOffsets[i] = in.readByte();
            }
            maxColumnCnt = 0;
            seconds = new byte[offsetNum][];
            for (int i = 0; i < offsetNum; i++) {
                short subLen = in.readShort();
                seconds[i] = new byte[subLen];
                for (int j = 0; j < subLen; j++) {
                    seconds[i][j] = in.readByte();
                    maxColumnCnt++;
                }
            }
        }
    }

    @Override
    public void reset() {
        columnFound = 0;
    }

    @Override
    public boolean filterRowKey(byte[] buffer, int offset, int length) {
        if (tagIdList == null || tagIdList.length == 0) {
            return false;
        }
        int low;
        int mid;
        int high;
        boolean found;
        int filersCnt = tagIdList.length;
        int tagIdx = 0;
        int pos = 11;
        while (pos + 6 < length) {
            if (tagIdList[tagIdx][1] == buffer[pos + 1] && tagIdList[tagIdx][0] == buffer[pos]) {
                if (valueIdBytesList[tagIdx] != null) {
                    int idx = 0;
                    found = false;
                    byte[] valueId = valueIdBytesList[tagIdx];
                    while (idx + 4 <= valueId.length) {
                        if (valueId[idx + 3] == buffer[pos + 5]
                                && valueId[idx + 2] == buffer[pos + 4]
                                && valueId[idx + 1] == buffer[pos + 3]
                                && valueId[idx] == buffer[pos + 2]) {
                            found = true;
                            break;
                        }
                        idx += 4;
                    }
                    if (!found) {
                        return true;
                    }
                } else {
                    int vid = (buffer[pos + 2] & 0xFF) << 24;
                    vid += (buffer[pos + 3] & 0xFF) << 16;
                    vid += (buffer[pos + 4] & 0xFF) << 8;
                    vid += buffer[pos + 5] & 0xFF;
                    int[] valIds = valueIdsList[tagIdx];
                    low = 0;
                    high = valIds.length - 1;
                    found = false;
                    int midVal;
                    while (low <= high) {
                        mid = (low + high) >>> 1;
                        midVal = valIds[mid];
                        if (midVal < vid) {
                            low = mid + 1;
                        } else if (midVal > vid) {
                            high = mid - 1;
                        } else {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return true;
                    }
                }
                tagIdx++;
                if (tagIdx == filersCnt) {
                    break;
                }
            }
            pos += 6;
        }
        if (tagIdx != filersCnt) {
            return true;
        }
        return false;
    }

    @Override
    public boolean filterAllRemaining() {
        return false;
    }

    @Override
    public ReturnCode filterKeyValue(KeyValue v) {
        if (secondOffsets != null) {
            if (columnFound == maxColumnCnt) {
                return ReturnCode.NEXT_ROW;
            }
            byte[] buffer = v.getBuffer();
            int qOffset = v.getQualifierOffset();
            byte secondOffset = buffer[qOffset];
            byte second = buffer[qOffset + 1];
            int low = 0;
            int mid;
            int high = secondOffsets.length - 1;
            int position = -1;
            byte midVal;
            while (low <= high) {
                mid = (low + high) >>> 1;
                midVal = secondOffsets[mid];
                if (midVal < secondOffset) {
                    low = mid + 1;
                } else if (midVal > secondOffset) {
                    high = mid - 1;
                } else {
                    position = mid;
                    break;
                }
            }
            if (position >= 0) {
                byte[] subSeconds = seconds[position];
                low = 0;
                high = subSeconds.length - 1;
                boolean found = false;
                while (low <= high) {
                    mid = (low + high) >>> 1;
                    midVal = subSeconds[mid];
                    if (midVal < second) {
                        low = mid + 1;
                    } else if (midVal > second) {
                        high = mid - 1;
                    } else {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    columnFound++;
                    return ReturnCode.INCLUDE_AND_NEXT_COL;
                } else {
                    return ReturnCode.NEXT_COL;
                }
            } else {
                return ReturnCode.NEXT_COL;
            }
        } else {
            return ReturnCode.INCLUDE_AND_NEXT_COL;
        }
    }

    @Override
    public KeyValue transform(KeyValue v) {
        return v;
    }

    @Override
    public void filterRow(List<KeyValue> kvs) {

    }

    @Override
    public boolean hasFilterRow() {
        return false;
    }

    @Override
    public boolean filterRow() {
        return false;
    }

    @Override
    public KeyValue getNextKeyHint(KeyValue currentKV) {
        return null;
    }

}
