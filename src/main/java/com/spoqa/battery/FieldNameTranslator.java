/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

public class FieldNameTranslator {

    private FieldNameTransformer mRemoteTransformer;
    private FieldNameTransformer mLocalTransformer;

    public FieldNameTranslator(FieldNameTransformer remote, FieldNameTransformer local) {
        mRemoteTransformer = remote;
        mLocalTransformer = local;
    }

    public String remoteToLocal(String name) {
        if (mRemoteTransformer == null || mLocalTransformer == null)
            return name;

        return mLocalTransformer.encode(mRemoteTransformer.decode(name));
    }

    public String localToRemote(String name) {
        if (mRemoteTransformer == null || mLocalTransformer == null)
            return name;

        return mRemoteTransformer.encode(mLocalTransformer.decode(name));
    }

}
