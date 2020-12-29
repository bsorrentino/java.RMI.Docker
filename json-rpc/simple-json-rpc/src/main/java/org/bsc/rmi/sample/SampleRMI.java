package org.bsc.rmi.sample;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;


@JsonRpcService
public interface SampleRMI {

    @JsonRpcMethod
    String justPass( @JsonRpcParam("toPass") String toPass);

    @JsonRpcMethod
    String getInfo();

}