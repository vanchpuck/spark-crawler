//package org.izolotov.crawler.fetch;
//
//import java.io.Serializable;
//import java.util.HashMap;
//import java.util.Map;
////import java.util.Optional;
//
//import com.google.common.base.Optional;
//import org.izolotov.crawler.Flag;
//import org.izolotov.crawler.WebPage;
//
//public class FetchStatus implements Serializable {
//
////    public enum Flag implements Serializable {
////        NOT_FETCHED_YET(0),
////        SUCCESS(1),
////        REDIRECT(2),
////        FAIL(3);
////
////        private int intCode;
////
////        Flag(int code) {
////            intCode = code;
////        }
////
////        public void setStatus(WebPage page) {
////            page.getFetchStatus().setFlag(this);
////        }
////
////        public void setStatus(WebPage page, String message) {
////            page.getFetchStatus().setFlag(this);
////            page.getFetchStatus().getInfo().put(this.toString(), message);
////        }
////
////        public Optional<String> getStatusMessage(WebPage page) {
//////			return Optional.ofNullable(page.getFetchStatus().getInfo().get(this.toString()));
////            return Optional.fromNullable(page.getFetchStatus().getInfo().get(this.toString()));
////        }
////
////        public boolean check(WebPage page) {
////            return this == page.getFetchStatus().getFlag();
////        }
////
////        public int getCode() {
////            return intCode;
////        }
////
////        @Override
////        public String toString() {
////            return name();
////        }
////    }
//
//    private Flag code;
//    private Map<String, String> info;
//
//    public static final FetchStatus of(Flag flag) {
//        return new FetchStatus(flag);
//    }
//
//    protected FetchStatus(Flag code) {
//        this.code = code;
//        this.info = new HashMap<>(4);
//    }
//
//    public void setFlag(Flag code) {
//        this.code = code;
//    }
//
//    public Flag getFlag() {
//        return code;
//    }
//
//    public FetchStatus putInfo(String key, String info) {
//        this.info.put(key, info);
//        return this;
//    }
//
//    public Map<String, String> getInfo() {
//        return info;
//    }
//}
