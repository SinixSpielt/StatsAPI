package de.sinixspielt.statsapi.file;

/*
Class created on 01.03.2019 by SinixSpielt
 * */

public class FileManager {

	private DatabaseFile databaseFile;
	
	public FileManager() {
		this.databaseFile = new DatabaseFile();
	}
	
	public DatabaseFile getDatabaseFile() {
		return databaseFile;
	}
}