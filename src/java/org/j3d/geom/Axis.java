/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                              Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.media.j3d.*;

import javax.vecmath.Color3f;

// Application specific imports

/**
 * Representation of a set of axis around the coordinates.
 * <p>
 *
 * Each axis is color coordinated and the length can be adjusted.
 * <p>
 * X axis: Red<br>
 * Y axis: Green<br>
 * Z axis: Blue
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class Axis extends Group
{
    /** The default length of the axis */
    private static final int DEFAULT_AXIS_LENGTH = 5;

    /**
     * Create a default axis object with each item length 5
     */
    public Axis()
    {
        int format = GeometryArray.COORDINATES | GeometryArray.NORMALS;
        BoxGenerator gen = new BoxGenerator(0.05f, 0.05f, 10f);

        float[] coords = gen.generateUnindexedCoordinates();
        float[] normals = gen.generateUnindexedNormals();
        int vertex_count = gen.getVertexCount();

        QuadArray x_array = new QuadArray(vertex_count, format);
        x_array.setCoordinates(0, coords);
        x_array.setNormals(0, normals);

        gen.setDimensions(0.05f, 10f, 0.05f);

        coords = gen.generateUnindexedCoordinates();
        normals = gen.generateUnindexedNormals();

        QuadArray y_array = new QuadArray(vertex_count, format);
        y_array.setCoordinates(0, coords);
        y_array.setNormals(0, normals);

        gen.setDimensions(10f, 0.05f, 0.05f);

        coords = gen.generateUnindexedCoordinates();
        normals = gen.generateUnindexedNormals();

        QuadArray z_array = new QuadArray(vertex_count, format);
        z_array.setCoordinates(0, coords);
        z_array.setNormals(0, normals);

        Color3f blue = new Color3f(0, 0, 0.8f);
        Material blue_material = new Material();
        blue_material.setDiffuseColor(blue);

        Color3f red = new Color3f(0.8f,0, 0);
        Material red_material = new Material();
        red_material.setDiffuseColor(red);

        Color3f green = new Color3f(0, 0.8f, 0);
        Material green_material = new Material();
        green_material.setDiffuseColor(green);

        Appearance x_app = new Appearance();
        x_app.setMaterial(red_material);

        Appearance y_app = new Appearance();
        y_app.setMaterial(green_material);

        Appearance z_app = new Appearance();
        z_app.setMaterial(blue_material);

        Shape3D x_shape = new Shape3D();
        x_shape.setAppearance(x_app);
        x_shape.addGeometry(x_array);

        Shape3D y_shape = new Shape3D();
        y_shape.setAppearance(y_app);
        y_shape.addGeometry(y_array);

        Shape3D z_shape = new Shape3D();
        z_shape.setAppearance(z_app);
        z_shape.addGeometry(z_array);

        addChild(x_shape);
        addChild(y_shape);
        addChild(z_shape);
    }
}