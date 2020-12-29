package org.bsc.rmi.sample;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;

public class SampleRMITest {

    /**
     * get method's annotations by type, evaluating also if annotation
     * is eventually present in the same method from inherited interfaces
     *
     * @param m method to evaluate
     * @return Optional containing annotation if found
     */
    <A extends Annotation> Optional<A> getMethodAnnotationByTpe( Method m, Class<A> annotationClass ) {

        final Annotation[] directs = m.getDeclaredAnnotations();
        if( directs!=null  && directs.length > 0 ) {
            for( Annotation direct : directs ) {
                if( direct.annotationType().equals(annotationClass))
                    return (Optional<A>) Optional.of(direct);
            }
        }

        final Class<?> interfaces[] = m.getDeclaringClass().getInterfaces();
        if( interfaces != null && interfaces.length > 0 ) {
            for (Class<?> ifc : interfaces) {
                try {
                    final Method mm = ifc.getMethod(m.getName(), m.getParameterTypes());
                    final Annotation[] indirects = mm.getDeclaredAnnotations();
                    for( Annotation indirect : indirects ) {
                        if( indirect.annotationType().equals(annotationClass))
                            return (Optional<A>) Optional.of(indirect);
                    }
                } catch (NoSuchMethodException e) {
                    // skip
                }
            }
        }
        return empty();
    }

    @Test
    public void testAnnotations() throws Exception {

        final Method m = SampleRMIServer.class.getMethod("justPass", String.class);

        assertNotNull(m);
        assertEquals( SampleRMIServer.class, m.getDeclaringClass() );

        final Optional<JsonRpcMethod> annotation = getMethodAnnotationByTpe(m, JsonRpcMethod.class);

        assertTrue( annotation.isPresent() );
        assertEquals( JsonRpcMethod.class, annotation.get().annotationType() );
    }
}
