package ch.eth.ir.indexserver.utilities;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import ch.eth.ir.indexserver.index.IndexAPI;

public class IndexReaderBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(IndexAPI.class).to(IndexAPI.class).in(Singleton.class);
    }
}
