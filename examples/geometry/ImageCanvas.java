/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;

// Local imports
// none

/**
 * Simple overridden canvas to draw an image on
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ImageCanvas extends Canvas
{
    private Image image;

    public void setImage(Image img)
    {
        image = img;

        setSize(img.getWidth(null), img.getHeight(null));
        invalidate();
    }

    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        if(image == null)
            g.drawLine(0, 0, getWidth(), getHeight());
        else
        {
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(image, 0, 0, null);
        }
    }
}
