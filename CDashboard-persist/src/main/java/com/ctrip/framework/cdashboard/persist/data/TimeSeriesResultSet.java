package com.ctrip.framework.cdashboard.persist.data;

import com.ctrip.framework.cdashboard.persist.dao.DataFragment;

import java.io.IOException;
import java.util.List;

public class TimeSeriesResultSet implements DataPointStream {
    private List<DataFragment> dataFragments;

    public TimeSeriesResultSet(List<DataFragment> dataFragments) {
        this.dataFragments = dataFragments;
    }

    @Override
    public void close() {
        for (int i = 0; i < dataFragments.size(); i++) {
            try {
                dataFragments.get(i).getTimeSeriesResultFragment().close();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    @Override
    public void downSample() throws IOException {
        try {
            if (dataFragments != null && dataFragments.size() > 0) {
                for (DataFragment dataFragment : dataFragments) {
                    DataPointStream dataPointStream = null;
                    try {
                        dataPointStream = dataFragment.getTimeSeriesResultFragment();
                        dataPointStream.downSample();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (dataPointStream != null) {
                            dataPointStream.close();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            close();
        }

    }
}
