package kizwid.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import java.util.HashSet;
import java.util.Properties;

/**
 * User: kizwid
 * Date: 2012-04-26
 */
public class EncryptedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private final static Logger logger = LoggerFactory.getLogger(EncryptedPropertyPlaceholderConfigurer.class);

    //cryptography key is passed in by what ever starts system (or sure-fire plugin for automated tests)
    private final static String ENCRYPT_KEY = System.getProperty("encrypt.key", "EFEB34A0247110600B98B9CEBE96AB7E"); //default value so I can run test manually

    //for Jndi based properties
    private String jndiPrefix = "java:comp/env/";
    private JndiTemplate jndiTemplate = new JndiTemplate();

    @Override
    protected  String convertPropertyValue(String originalValue){
        final String ENCRYPT_MARKER_START="ENC(";
        final String ENCRYPT_MARKER_END=")";
        if(originalValue.startsWith(ENCRYPT_MARKER_START) && originalValue.endsWith(ENCRYPT_MARKER_END)){
            logger.debug("decrypting value [{}]", originalValue);
            try {
                final String encrypted =
                        originalValue.substring(ENCRYPT_MARKER_START.length(),
                                originalValue.length()-ENCRYPT_MARKER_END.length());

                final String decrypted;
                if(ENCRYPT_KEY.length()> 0){
                    decrypted = Cryptography.decrypt(encrypted,ENCRYPT_KEY);
                }else{
                    decrypted = Cryptography.decrypt(encrypted);//TODO nice to delegate management of crypography to 3rd party, but reading file each time - is this best way?
                }
                return decrypted;
            } catch (Exception e) {
                logger.error("unable to decrypt value [{}]", new Object[]{originalValue}, e);
                throw new IllegalArgumentException(String.format("unable to decrypt value [%s]", originalValue),e);
            }
        } else {
            return originalValue;
        }
    }

    /*if a jndi property has been set then use it*/
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String value;
        value = resolveJndiPlaceholder(placeholder);
        if (value == null) {
            value = super.resolvePlaceholder(placeholder, props);
        }
        return value;
    }

    private String resolveJndiPlaceholder(String placeholder) {
        try {
            return jndiTemplate.lookup(jndiPrefix + placeholder, String.class);
        } catch (NamingException e) {
            // ignore
        }
        return null;
    }

    public void setLocation(Resource location) {
        super.setLocation(fixLocation(location));
    }

    public void setLocations(Resource[] locations) {
        //for each location that still has unresolved placeholders
        //look for the appropriate value in jndi
        Resource[] fixedLocations = new Resource[locations.length];
        for (int n = 0; n < locations.length; n++) {
            fixedLocations[n]=fixLocation(locations[n]);
        }
        super.setLocations(fixedLocations);
    }

    private Resource fixLocation(Resource location) {
        if( location instanceof ClassPathResource){
            String path = ((ClassPathResource)location).getPath();
            String fixedPath = parseStringValue(path, new Properties(), new HashSet());
            location = new ClassPathResource(fixedPath);
        }
        return location;
    }

}
