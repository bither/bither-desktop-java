/**
 * Copyright 2012 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.utils;

import net.bither.bitherj.core.Tx;
import net.bither.viewsystem.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public final class ImageLoader {
    private static final Logger log = LoggerFactory.getLogger(ImageLoader.class);


    public static final String BITHER_ICON_FILE = "/images/bither_icon.png";
    public static final String SHAPE_TRIANGLE_ICON_FILE = "/images/shapeTriangle.png";
    public static final String SHAPE_SQUARE_ICON_FILE = "/images/shapeSquare.png";
    public static final String SHAPE_PENTAGON_ICON_FILE = "/images/shapePentagon.png";
    public static final String SHAPE_HEXAGON_ICON_FILE = "/images/shapeHexagon.png";
    public static final String TICK_ICON_FILE = "/images/tick.png";
    public static final String TRANSACTIONS_ICON_FILE = "/images/transactions.png";

    public static final ImageIcon CHECK_MARK = ImageLoader.createImageIcon("/images/checkmark.png");
    public static final ImageIcon CHECK_FAILED = ImageLoader.createImageIcon("/images/check_failed.png");


    public static final String PROGRESS_0_ICON_FILE = "/images/circleProgress0.png";
    private static final String PROGRESS_1_ICON_FILE = "/images/circleProgress1.png";
    private static final String PROGRESS_2_ICON_FILE = "/images/circleProgress2.png";
    private static final String PROGRESS_3_ICON_FILE = "/images/circleProgress3.png";
    private static final String PROGRESS_4_ICON_FILE = "/images/circleProgress4.png";
    private static final String PROGRESS_5_ICON_FILE = "/images/circleProgress5.png";
    private static final String RTL_PROGRESS_1_ICON_FILE = "/images/circleProgress1.png";
    private static final String RTL_PROGRESS_2_ICON_FILE = "/images/circleProgress2.png";
    private static final String RTL_PROGRESS_3_ICON_FILE = "/images/circleProgress3.png";
    private static final String RTL_PROGRESS_4_ICON_FILE = "/images/circleProgress4.png";
    private static final String RTL_PROGRESS_5_ICON_FILE = "/images/circleProgress5.png";

    private static final String PICKAXE_ICON_FILE = "/images/pickaxe.png";
    private static final String SMALL_EXCLAMATION_MARK_ICON_FILE = "/images/smallExclamationMark.png";

    public static ImageIcon pickaxeIcon = ImageLoader.createImageIcon(PICKAXE_ICON_FILE);

    private static ImageIcon tickIcon = ImageLoader.createImageIcon(TICK_ICON_FILE);
    private static ImageIcon progress0Icon = ImageLoader.createImageIcon(PROGRESS_0_ICON_FILE);
    private static ImageIcon progress1Icon = ImageLoader.createImageIcon(PROGRESS_1_ICON_FILE);
    private static ImageIcon progress2Icon = ImageLoader.createImageIcon(PROGRESS_2_ICON_FILE);
    private static ImageIcon progress3Icon = ImageLoader.createImageIcon(PROGRESS_3_ICON_FILE);
    private static ImageIcon progress4Icon = ImageLoader.createImageIcon(PROGRESS_4_ICON_FILE);
    private static ImageIcon progress5Icon = ImageLoader.createImageIcon(PROGRESS_5_ICON_FILE);
    private static ImageIcon rtlProgress1Icon = ImageLoader.createImageIcon(RTL_PROGRESS_1_ICON_FILE);
    private static ImageIcon rtlProgress2Icon = ImageLoader.createImageIcon(RTL_PROGRESS_2_ICON_FILE);
    private static ImageIcon rtlProgress3Icon = ImageLoader.createImageIcon(RTL_PROGRESS_3_ICON_FILE);
    private static ImageIcon rtlProgress4Icon = ImageLoader.createImageIcon(RTL_PROGRESS_4_ICON_FILE);
    private static ImageIcon rtlProgress5Icon = ImageLoader.createImageIcon(RTL_PROGRESS_5_ICON_FILE);


    private static ImageIcon shapeTriangleIcon;
    private static ImageIcon shapeSquareIcon;
    private static ImageIcon shapeHeptagonIcon;
    private static ImageIcon shapeHexagonIcon;


    static {
        shapeTriangleIcon = ImageLoader.createImageIcon(ImageLoader.SHAPE_TRIANGLE_ICON_FILE);
        shapeSquareIcon = ImageLoader.createImageIcon(ImageLoader.SHAPE_SQUARE_ICON_FILE);
        shapeHeptagonIcon = ImageLoader.createImageIcon(ImageLoader.SHAPE_PENTAGON_ICON_FILE);
        shapeHexagonIcon = ImageLoader.createImageIcon(ImageLoader.SHAPE_HEXAGON_ICON_FILE);

    }

    /**
     * Utility class should not have a public constructor
     */
    private ImageLoader() {
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon createImageIcon(String path) {
        if (path == null) {
            return null;
        }

        java.net.URL imgURL = MainFrame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            log.error("createImageIcon: Could not find file: " + path);
            return null;
        }
    }


    public static ImageIcon getBuildingIcon(Tx transaction, int numberOfBlocksEmbedded) {
        if (numberOfBlocksEmbedded < 0) {
            numberOfBlocksEmbedded = 0;
        }
        if (numberOfBlocksEmbedded > 6) {
            numberOfBlocksEmbedded = 6;
        }

        boolean isLeftToRight = ComponentOrientation.getOrientation(LocaliserUtils.getLocale()).isLeftToRight();

        switch (numberOfBlocksEmbedded) {
            case 0: {
                return getConfidenceIcon(transaction);
            }
            case 1: {
                if (isLeftToRight) {
                    return progress1Icon;
                } else {
                    return rtlProgress1Icon;
                }
            }
            case 2: {
                if (isLeftToRight) {
                    return progress2Icon;
                } else {
                    return rtlProgress2Icon;
                }
            }
            case 3: {
                if (isLeftToRight) {
                    return progress3Icon;
                } else {
                    return rtlProgress3Icon;
                }
            }
            case 4: {
                if (isLeftToRight) {
                    return progress4Icon;
                } else {
                    return rtlProgress4Icon;
                }
            }
            case 5: {
                if (isLeftToRight) {
                    return progress5Icon;
                } else {
                    return rtlProgress5Icon;
                }
            }
            case 6: {
                return tickIcon;
            }
            default:
                return getConfidenceIcon(transaction);
        }


    }


    private static ImageIcon getConfidenceIcon(Tx tx) {
        // By default return a triangle which indicates the least known.
        ImageIcon iconToReturn = shapeTriangleIcon;


        if (tx.getConfirmationCount() == 0) {
            return progress0Icon;
        }


        return iconToReturn;
    }

    public static ImageIcon getConfidenceIcon(int numberOfPeers) {
        // By default return a triangle which indicates the least known.
        ImageIcon iconToReturn;

        if (numberOfPeers >= 4) {
            return progress0Icon;
        } else {
            switch (numberOfPeers) {
                case 0:
                    iconToReturn = shapeTriangleIcon;
                    break;
                case 1:
                    iconToReturn = shapeSquareIcon;
                    break;
                case 2:
                    iconToReturn = shapeHeptagonIcon;
                    break;
                case 3:
                    iconToReturn = shapeHexagonIcon;
                    break;
                default:
                    iconToReturn = shapeTriangleIcon;
            }
        }
        return iconToReturn;
    }
}
