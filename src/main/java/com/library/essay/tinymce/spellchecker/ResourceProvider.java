package com.library.essay.tinymce.spellchecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class ResourceProvider {
	private static ResourceProvider INSTANCE;

	private File dictionariesRootFolder;

	private ResourceProvider(String dictionariesDirectory) {

		if (dictionariesDirectory == null) {
			throw new IllegalStateException(
					"There is no dictionaries.directory property in the 'jspellchecker.properties'");
		}

		dictionariesRootFolder = new File(dictionariesDirectory);

	}

	public static ResourceProvider getInstance(String dictionariesDirectory) {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		synchronized (ResourceProvider.class) {
			INSTANCE = new ResourceProvider(dictionariesDirectory);
		}
		return INSTANCE;
	}

	
	public static File getClassPathResourceFile(String resourceName,
			Class accessor) {
		if (resourceName.startsWith("/")) {
			return new File(accessor.getResource(resourceName).getFile());
		}
		return getClassFolderResourceFile(resourceName, accessor);
	}

	public static InputStream getClassPathResourceInputStream(
			String resourceName, Class accessor) {
		if (resourceName.startsWith("/")) {
			return accessor.getResourceAsStream(resourceName);
		}
		return accessor.getResourceAsStream(resolveResourcePath(resourceName,
				accessor));
	}

	private static File getClassFolderResourceFile(String resourceName,
			Class clazz) {
		final String resolvedResourcePath = resolveResourcePath(resourceName,
				clazz);
		return new File(clazz.getResource(resolvedResourcePath).getFile());
	}

	private static String resolveResourcePath(String resourceName, Class clazz) {
		return new StringBuilder().append("/")
				.append(clazz.getName().replace(".", "/")).append("/")
				.append(resourceName).toString();
	}

	public File getDictionaryFile(String... pathComponents) {
		return new File(dictionariesRootFolder, join(File.separator,
				pathComponents));
	}

	public static String join(String joiner, String... components) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < components.length; i++) {
			if (i > 0) {
				result.append(joiner);
			}
			result.append(components[i]);
		}
		return result.toString();
	}
}
