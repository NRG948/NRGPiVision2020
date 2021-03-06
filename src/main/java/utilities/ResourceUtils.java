/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package utilities;

import java.io.IOException;
import java.io.InputStream;

/**
 * Add your docs here.
 */
public class ResourceUtils {
    /**
     * loads a resource as a json string
     * @param classObj The class object for the object loading the resource
     * @param resource The name of the resource to load
     * @return Returns the resource as a json string
     * @throws IOException
     */
    public static String loadJsonResource(Class<?> classObj, String resource) throws IOException {
        InputStream stream = classObj.getResourceAsStream(resource);
        return new String (stream.readAllBytes());
    }
}
