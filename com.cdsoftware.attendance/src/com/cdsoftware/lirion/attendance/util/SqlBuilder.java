/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Casa del Software                                                 *
 **********************************************************************/

package com.cdsoftware.lirion.attendance.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to build SQL strings by reading external .sql files or adding statements programmatically.
 * It handles basic cleaning such as removing SQL comments (lines starting with "--") and trimming whitespace.
 *
 * Example usage:
 * <pre>
 * String sql = SqlBuilder.builder().file("queries/my-query.sql").build();
 * </pre>
 */
public class SqlBuilder {

	private StringBuilder sql;
	private Class<?> contextClass;

	private SqlBuilder(StringBuilder sql, Class<?> contextClass) {
		this.sql = sql;
		this.contextClass = contextClass;
	}

	/**
	 * Creates a new builder
	 * 
	 * @return New builder object
	 */
	public static SqlBuilder builder() {
		return new SqlBuilder(new StringBuilder(), SqlBuilder.class);
	}

	/**
	 * Set the class context loader
	 * 
	 * @param contextClass
	 * @return New builder object
	 */
	public static SqlBuilder builder(Class<?> contextClass) {
		return new SqlBuilder(new StringBuilder(), contextClass);
	}

	/**
	 * Adds a a sql file to sql string. For comments in the sql file use "--"
	 * symbol.
	 * 
	 * @param path Sql path inside plugin
	 * @return Current builder
	 * @throws IOException When throws a error reading the file
	 */
	public SqlBuilder file(String path) throws IOException {
		InputStream resourceAsStream = contextClass.getClassLoader().getResourceAsStream(path);

		if (resourceAsStream == null) {
			throw new FileNotFoundException(String.format("Path: %s, Context: %s", path, contextClass));
		}

		StringBuilder copy = new StringBuilder(sql);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				processStatement(line, copy);
			}
		}

		return new SqlBuilder(copy, contextClass);
	}

	/**
	 * Adds a statement line to the current sql builder
	 * 
	 * @param statement A string sql
	 * @return Current builder
	 */
	public SqlBuilder statement(String statement) {
		StringBuilder copy = new StringBuilder(sql);

		processStatement(statement, copy);

		return new SqlBuilder(copy, contextClass);
	}

	private void processStatement(String statement, StringBuilder container) {
		statement = statement.trim();
		if (!statement.startsWith("--")) {
			container.append(statement).append(" ");
		}
	}

	/**
	 * Concats the whole path set
	 * 
	 * @return Sql string
	 */
	public String build() {
		return sql.toString().trim();
	}
}
