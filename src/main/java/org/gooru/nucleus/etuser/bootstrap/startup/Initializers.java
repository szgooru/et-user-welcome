package org.gooru.nucleus.etuser.bootstrap.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gooru.nucleus.etuser.app.components.ApplicationRegistry;

public class Initializers implements Iterable<Initializer> {

    private final Iterator<Initializer> internalIterator;
    
    public Initializers() {
        List<Initializer> initializers = new ArrayList<>();
        initializers.add(ApplicationRegistry.getInstance());
        internalIterator = initializers.iterator();
    }
    
    @Override
    public Iterator<Initializer> iterator() {
        return new Iterator<Initializer>() {

            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public Initializer next() {
                return internalIterator.next();
            }
        };
    }

}
