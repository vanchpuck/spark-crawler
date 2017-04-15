package org.izolotov.crawler;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.mapreduce.Job;

public class HbaseClient implements Configurable {

    private Configuration conf;

    public HbaseClient(String tableName) throws IOException {
        conf = HBaseConfiguration.create();
        Job job = Job.getInstance();
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, tableName);
        job.setOutputFormatClass(TableOutputFormat.class);
        conf.addResource(job.getConfiguration());
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

}
