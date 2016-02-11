/**
 * eclipse-p2repo-index by Worldline
 *
 * Copyright (C) 2016 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.worldline.mojo.p2repoindex.mojos;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Special SLF4J logger implementation, for Apache Maven loggers
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class MavenLoggerWrapper implements Logger {

	private final Log log;

	public MavenLoggerWrapper(Log log) {
		this.log = log;
	}

	public String getName() {
		return this.log.toString();
	}

	public boolean isTraceEnabled() {
		return this.isDebugEnabled();
	}

	public void trace(String msg) {
		this.debug(msg);
	}

	public void trace(String format, Object arg) {
		this.debug(format, arg);
	}

	public void trace(String format, Object arg1, Object arg2) {
		this.debug(format, arg1, arg2);
	}

	public void trace(String format, Object... arguments) {
		this.debug(format, arguments);
	}

	public void trace(String msg, Throwable t) {
		this.debug(msg, t);
	}

	public boolean isTraceEnabled(Marker marker) {
		return this.isDebugEnabled(marker);
	}

	public void trace(Marker marker, String msg) {
		this.debug(marker, msg);
	}

	public void trace(Marker marker, String format, Object arg) {
		this.debug(marker, format, arg);
	}

	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		this.debug(marker, format, arg1, arg2);
	}

	public void trace(Marker marker, String format, Object... argArray) {
		this.debug(marker, format, argArray);
	}

	public void trace(Marker marker, String msg, Throwable t) {
		this.debug(marker, msg, t);
	}

	public boolean isDebugEnabled() {
		return this.log.isDebugEnabled();
	}

	public void debug(String msg) {
		this.log.debug(msg);
	}

	public void debug(String format, Object arg) {
		this.log.debug(String.format(format, arg));
	}

	public void debug(String format, Object arg1, Object arg2) {
		this.log.debug(String.format(format, arg1, arg2));
	}

	public void debug(String format, Object... arguments) {
		this.log.debug(String.format(format, arguments));
	}

	public void debug(String msg, Throwable t) {
		this.log.debug(msg, t);
	}

	public boolean isDebugEnabled(Marker marker) {
		return this.isDebugEnabled();
	}

	public void debug(Marker marker, String msg) {
		this.debug(msg);
	}

	public void debug(Marker marker, String format, Object arg) {
		this.debug(format, arg);
	}

	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		this.debug(format, arg1, arg2);

	}

	public void debug(Marker marker, String format, Object... arguments) {
		this.debug(format, arguments);

	}

	public void debug(Marker marker, String msg, Throwable t) {
		this.debug(msg, t);
	}

	public boolean isInfoEnabled() {
		return this.log.isInfoEnabled();
	}

	public void info(String msg) {
		this.log.info(msg);
	}

	public void info(String format, Object arg) {
		this.log.info(String.format(format, arg));
	}

	public void info(String format, Object arg1, Object arg2) {
		this.log.info(String.format(format, arg1, arg2));
	}

	public void info(String format, Object... arguments) {
		this.log.info(String.format(format, arguments));
	}

	public void info(String msg, Throwable t) {
		this.log.info(msg, t);

	}

	public boolean isInfoEnabled(Marker marker) {
		return isInfoEnabled();
	}

	public void info(Marker marker, String msg) {
		this.info(msg);
	}

	public void info(Marker marker, String format, Object arg) {
		this.info(format, arg);
	}

	public void info(Marker marker, String format, Object arg1, Object arg2) {
		this.info(format, arg1, arg2);
	}

	public void info(Marker marker, String format, Object... arguments) {
		this.info(format, arguments);
	}

	public void info(Marker marker, String msg, Throwable t) {
		this.info(msg, t);
	}

	public boolean isWarnEnabled() {
		return this.log.isWarnEnabled();
	}

	public void warn(String msg) {
		this.log.warn(msg);
	}

	public void warn(String format, Object arg) {
		this.log.warn(String.format(format, arg));
	}

	public void warn(String format, Object... arguments) {
		this.log.warn(String.format(format, arguments));
	}

	public void warn(String format, Object arg1, Object arg2) {
		this.log.warn(String.format(format, arg1, arg2));
	}

	public void warn(String msg, Throwable t) {
		this.log.warn(msg, t);
	}

	public boolean isWarnEnabled(Marker marker) {
		return this.isWarnEnabled();
	}

	public void warn(Marker marker, String msg) {
		this.warn(msg);
	}

	public void warn(Marker marker, String format, Object arg) {
		this.warn(format, arg);
	}

	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		this.warn(format, arg1, arg2);
	}

	public void warn(Marker marker, String format, Object... arguments) {
		this.warn(format, arguments);
	}

	public void warn(Marker marker, String msg, Throwable t) {
		this.warn(msg, t);
	}

	public boolean isErrorEnabled() {
		return this.log.isErrorEnabled();
	}

	public void error(String msg) {
		this.log.error(msg);
	}

	public void error(String format, Object arg) {
		this.log.error(String.format(format, arg));
	}

	public void error(String format, Object arg1, Object arg2) {
		this.log.error(String.format(format, arg1, arg2));
	}

	public void error(String format, Object... arguments) {
		this.log.error(String.format(format, arguments));
	}

	public void error(String msg, Throwable t) {
		this.log.error(msg, t);
	}

	public boolean isErrorEnabled(Marker marker) {
		return this.isErrorEnabled();
	}

	public void error(Marker marker, String msg) {
		this.error(msg);
	}

	public void error(Marker marker, String format, Object arg) {
		this.error(format, arg);
	}

	public void error(Marker marker, String format, Object arg1, Object arg2) {
		this.error(format, arg1, arg2);
	}

	public void error(Marker marker, String format, Object... arguments) {
		this.error(format, arguments);
	}

	public void error(Marker marker, String msg, Throwable t) {
		this.error(msg, t);
	}

}
