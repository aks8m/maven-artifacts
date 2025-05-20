package dev.ikm.maven.toolkit.isolated.controller;

import dev.ikm.maven.toolkit.isolated.boundary.IsolatedTinkarMojo;
import dev.ikm.maven.toolkit.isolated.entity.IsolatedField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

public class IsolationReceiver {

	private final IsolationFieldSerializer isolationFieldSerializer;
	private final String canonicalName;

	public IsolationReceiver(String isolatedFieldsDirectory, String canonicalName) {
		this.isolationFieldSerializer = new IsolationFieldSerializer(Path.of(isolatedFieldsDirectory));
		this.canonicalName = canonicalName;
	}

	public IsolatedTinkarMojo runnableInstance() {
		IsolatedTinkarMojo isolatedTinkarMojo = null;
		try {
			Class<IsolatedTinkarMojo> isolatedTinkarMojoClass = (Class<IsolatedTinkarMojo>) Class.forName(canonicalName);
			 isolatedTinkarMojo = isolatedTinkarMojoClass.getDeclaredConstructor(null).newInstance(null);
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException |
				 ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		List<IsolatedField> isolatedFields = isolationFieldSerializer.deserializeIsolatedFields();
		injectIsolatedFields(isolatedTinkarMojo, isolatedFields);
		return isolatedTinkarMojo;
	}

	private static void injectIsolatedFields(IsolatedTinkarMojo isolatedTinkarMojo, List<IsolatedField> isolatedFields) {
		//Inject Isolated Fields into class fields (super)
		Class<?> clazz =  isolatedTinkarMojo.getClass();
		for (Field field : clazz.getFields()) {
			isolatedFields.forEach(isolatedField -> {
				if (isolatedField.name().equals(field.getName())) {
					field.setAccessible(true);
					try {
						field.set(isolatedTinkarMojo, isolatedField.object());
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
		//Inject Isolated Fields into class fields (instance)
		for (Field field : clazz.getDeclaredFields()) {
			isolatedFields.forEach(isolatedField -> {
				if (isolatedField.name().equals(field.getName())) {
					field.setAccessible(true);
					try {
						field.set(isolatedTinkarMojo, isolatedField.object());
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}
}
