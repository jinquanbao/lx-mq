package com.laoxin.mq.client.impl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerAddressProvider {

    private List<InetSocketAddress> addresses;
    private Random random = new Random();

    public ServerAddressProvider(String serviceUrl){
        this.addresses = new ArrayList<>();
        final String[] split = serviceUrl.split(",");
        //this.address = InetSocketAddress.createUnresolved(split[0], Integer.parseInt(split[1]));
        try {
            for(String url: split){
                URI uri = new URI(url);
                addresses.add(new InetSocketAddress(uri.getHost(),uri.getPort()));
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public InetSocketAddress getAddress(){
        if(addresses.size() == 1){
            return addresses.get(0);
        }
        if(addresses.isEmpty()){
            return null;
        }
        return addresses.get(random.nextInt(addresses.size()));
    }

}
