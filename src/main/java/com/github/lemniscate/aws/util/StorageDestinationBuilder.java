package com.github.lemniscate.aws.util;

import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

public class StorageDestinationBuilder {

    private final Map<String, Object> params = Maps.newHashMap();
    private String bucket;
    private Date expiration;

    public S3StoragePolicyBuilder policyBuilder(Date expiration){
        this.expiration = expiration;
        return new S3StoragePolicyBuilder(expiration, this);
    }

    public StorageDestinationBuilder setKey(String fullFilename){
        params.put("key", fullFilename);
        return this;
    }

    public StorageDestinationBuilder set(String key, Object param){
        params.put(key, param);
        return this;
    }

    public StorageDestination build(){
        if( !params.containsKey("Content-Type") ){
            params.put("Content-Type", "");
        }
        return new StorageDestination(bucket, "POST", expiration, params);
    }

    public class StorageDestination{

        public StorageDestination(String url, String method, Date expiration, Map<String, Object> params) {
            this.url = url;
            this.method = method;
            this.expiration = expiration;
            this.params = params;
        }

        private final String url, method;
        private final Date expiration;
        private final Map<String, Object> params;

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public Date getExpiration() {
            return expiration;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }

    public static class S3StoragePolicyBuilder {

        private final StorageDestinationBuilder sdBuilder;
        private final S3PolicyBuilder policyBuilder;

        public S3StoragePolicyBuilder(Date expiration, StorageDestinationBuilder sdBuilder) {
            this.sdBuilder = sdBuilder;
            this.policyBuilder = new S3PolicyBuilder(expiration);
        }

        public S3StoragePolicyBuilder setBucket(String bucket){
            policyBuilder.equality("bucket", bucket);
            sdBuilder.bucket = bucket;
            return this;
        }

        public S3StoragePolicyBuilder setKeyPrefix(String prefix){
            policyBuilder.startsWith("key", prefix);
            return this;
        }

        // TODO make this param an enum
        public S3StoragePolicyBuilder setAcl(String acl){
            policyBuilder.equality("acl", acl);
            sdBuilder.params.put("acl", acl);
            return this;
        }

        public S3StoragePolicyBuilder setContentType(String contentType){
            // TODO figure out why this doesn't work
//            policyBuilder.startsWith("$Content-Type", contentType);
            sdBuilder.params.put("Content-Type", contentType);
            return this;
        }

        public S3StoragePolicyBuilder setRedirect(String url){
            policyBuilder.equality("success_action_redirect", url);
            sdBuilder.params.put("success_action_redirect", url);
            return this;
        }

        public StorageDestinationBuilder done(String accessKeyId, String secretKey){
            // TODO verify we have everything we need
            sdBuilder.params.put("policy", policyBuilder.buildAndBase64Encode());
            sdBuilder.params.put("signature", policyBuilder.buildSignature(secretKey));
            sdBuilder.params.put("AWSAccessKeyId", accessKeyId);
            return sdBuilder;
        }

    }

}
