package ch.eth.ir.indexserver.server.config;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.security.UserProperties;

public class SingletonBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(IndexAPI.class).to(IndexAPI.class).in(Singleton.class);
    }
}
