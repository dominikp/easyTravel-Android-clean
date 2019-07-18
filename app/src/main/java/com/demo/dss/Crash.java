/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: Crash.java
 * @date: Sep 11, 2015
 * @author: pfhbvl0
 */
package com.demo.dss;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Crash {
	private boolean isLoud;

	public Crash() {
	}

	public void boom() {
		isLoud = true;
		genException();
	}

	public void pop() {
		isLoud = false;
		genException();
	}

	private void genException() {
		genExceptionIn1();
	}

	private void genExceptionIn1() {
		genExceptionIn2();
	}

	private void genExceptionIn2() {
		genExceptionIn3();
	}

	private void genExceptionIn3() {
		genExceptionIn4();
	}

	private void genExceptionIn4() {
		genExceptionIn5();
	}

	private void genExceptionIn5() {
		if (isLoud) {
			genCrashNow();
		} else {
			genErrorNow();
		}
	}

	private void genErrorNow() {
		String[] array = new String[] {"easyTravel"};
		array[1].length();
	}

	private void genCrashNow() {
		throw new RuntimeException("Intentionally triggered crash! ID(24h): "+GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY));	//only use hours value, to create new original crashes after an hour!
	}

}
