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

package j3d.examples.texture;

// External imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;

// Local imports
import j3d.examples.common.DemoFrame;
import org.j3d.util.ImageUtils;

/**
 * ImageUtilsnstration of a mouse navigation in a world.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ImageUtilsDemo extends DemoFrame
    implements WindowListener,
               ItemListener
{
    /** The labels rendered on the frame */
    private JLabel sourceImage;
    private JLabel destImage;

    /** map of checkbox to filename string */
    private HashMap nameMap;

    public ImageUtilsDemo()
    {
        super("Image utils test");

        JPanel p1 = new JPanel(new GridLayout(1, 2));
        JPanel p2 = new JPanel(new BorderLayout());

        sourceImage = new JLabel("Image loading");
        destImage = new JLabel("Image Loading");

        p2.add(sourceImage, BorderLayout.CENTER);
        p2.add(new JLabel("Source Image"), BorderLayout.SOUTH);

        JPanel p3 = new JPanel(new BorderLayout());

        p3.add(destImage, BorderLayout.CENTER);
        p3.add(new JLabel("Copy Image"), BorderLayout.SOUTH);

        p1.add(p2);
        p1.add(p3);

        add(p1, BorderLayout.CENTER);

        JPanel p4 = new JPanel(new FlowLayout());

        nameMap = new HashMap();
        ButtonGroup grp = new ButtonGroup();
        JRadioButton button = new JRadioButton("GIF Image");
        button.addItemListener(this);
        grp.add(button);
        nameMap.put(button, "images/test_image.gif");
        p4.add(button);

        button = new JRadioButton("PNG Image");
        button.addItemListener(this);
        grp.add(button);
        nameMap.put(button, "images/test_image.png");
        p4.add(button);

        add(p4, BorderLayout.SOUTH);

        setSize(250, 200);
        setLocation(40, 40);
        addWindowListener(this);
    }

    public void loadImages(String name)
    {
        ImageIcon icon = new ImageIcon(name);

        sourceImage.setIcon(icon);
        sourceImage.setText(null);

        Image src = icon.getImage();

        BufferedImage dest = ImageUtils.createBufferedImage(src);

        try
        {
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(dest, 0);
            mt.waitForAll();
        }
        catch(InterruptedException ie)
        {
        }

        destImage.setIcon(new ImageIcon(dest));
        destImage.setText(null);
    }

    /**
     * Process the change of state request from the colour selector panel.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void itemStateChanged(ItemEvent evt)
    {
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            Object src = evt.getSource();
            String img_name = (String)nameMap.get(src);
            loadImages(img_name);
        }
    }


    public static void main(String[] args)
    {
        ImageUtilsDemo demo = new ImageUtilsDemo();
        demo.setVisible(true);
    }
}
