package br.com.sedentary.mouseless.mouse;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.logging.Logger;

/**
 * @author Rodrigo Gomes da Silva
 *
 */
public class MouseControll {
	private final static Logger LOGGER = Logger.getLogger(MouseControll.class.getName());
	
	Robot robot;
	
	public MouseControll() {
		try {
			this.robot = new Robot();
		} catch (AWTException e) {
			LOGGER.warning("Erro iniciando controle do mouse");
			e.printStackTrace();
		}
	}
	
	/**
	 * @param clickType
	 */
	public void click(MouseClickType clickType) {
		if (clickType.equals(MouseClickType.LEFT_DOWN)) {
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			
		} else if (clickType.equals(MouseClickType.LEFT_UP)) {
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		
		} else if (clickType.equals(MouseClickType.MID_DOWN)) {
			robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
		
		} else if (clickType.equals(MouseClickType.MID_UP)) {
			robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
			
		} else if (clickType.equals(MouseClickType.RIGHT_DOWN)) {
			robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
			
		} else if (clickType.equals(MouseClickType.RIGHT_UP)) {
			robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
		}
	}
	
	/**
	 * @param coords
	 */
	public void move(Integer[] coords) {
		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
		Integer x = (int) mouseLocation.getX();
		Integer y = (int) mouseLocation.getY();
		
		Integer aX = coords[0].intValue();
		Integer aY = coords[1].intValue();
		
		System.out.println("X: " + aX + ", Y: " + aY);
		
		x = x - aX;
		y = y + aY;
		
		robot.mouseMove(x, y);
	}
}
