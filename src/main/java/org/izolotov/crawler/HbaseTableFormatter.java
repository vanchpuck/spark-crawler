package org.izolotov.crawler;

import java.io.Serializable;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;

import org.izolotov.crawler.parse.TextDocument;
import scala.Tuple2;

public class HbaseTableFormatter implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6875922518768032570L;

    private static final String FETCH_FAMILY = "f";
    private static final String STATUS_FAMILY = "f";

    private enum Field {
        HTTP_STATUS_CODE(FETCH_FAMILY, "sc"),
        FETCH_STATUS_FLAG(STATUS_FAMILY, "ff");

        private byte[] family;
        private byte[] qualifier;

        Field(String family, String qualifier) {
            this.family = Bytes.toBytes(family);
            this.qualifier = Bytes.toBytes(qualifier);
        }

        public byte[] getQualifier() {
            return qualifier;
        }

        public byte[] getFamily() {
            return family;
        }
    }


    public Tuple2<ImmutableBytesWritable, Put> format(TextDocument doc) {
        Put put = new Put(Bytes.toBytes(doc.getUrl().toString()));
//        addColumn(put, Field.HTTP_STATUS_CODE, page.getHttpStatusCode());
        addColumn(put, Field.FETCH_STATUS_FLAG, doc.getFetchStatus().getFlag().getCode());
        return new Tuple2<>(new ImmutableBytesWritable(), put);
    }

    private static void addColumn(Put put, Field field, int value) {
        addColumn(put, field, Bytes.toBytes(value));
    }

    private static void addColumn(Put put, Field field, String value) {
        addColumn(put, field, Bytes.toBytes(value));
    }

    private static void addColumn(Put put, Field field, byte[] value) {
        put.addColumn(field.getFamily(), field.getQualifier(), value);
    }

}
