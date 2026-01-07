package org.jbake.parser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.IParse;
import com.vladsch.flexmark.util.ast.IRender;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
public class MarkdownEngine extends MarkupEngine {

	private static final Logger logger = LoggerFactory.getLogger(MarkdownEngine.class);
	private Set<Class<? extends Extension>> flexMarkExtentionClasses;

	private Set<Class<? extends Extension>> flexMarkOptionsHolderClasses;
	private Set<Class<? extends IRender>> flexMarkOptionsIRenderHolderClasses;
	private Set<Class<? extends IParse>> flexMarkOptionsIParseHolderClasses;

	public MarkdownEngine() {
		loadFlexMakExtensionClasses();
	}

	private void loadFlexMakExtensionClasses() {
		Reflections reflections = new Reflections("com.vladsch.flexmark.ext");
		flexMarkExtentionClasses = reflections.getSubTypesOf(Extension.class);

		Reflections reflectionsForOptions = new Reflections("com.vladsch.flexmark");
		flexMarkOptionsHolderClasses = reflectionsForOptions.getSubTypesOf(Extension.class);

		flexMarkOptionsIRenderHolderClasses = reflectionsForOptions.getSubTypesOf(IRender.class);
		flexMarkOptionsIParseHolderClasses = reflectionsForOptions.getSubTypesOf(IParse.class);

	}

	@Override
	public void processBody(final ParserContext context) {
		List<String> mdExts = context.getConfig().getMarkdownExtensions();
		Map<String, String> mdOptions = context.getConfig().getMarkdownOptions();

		int extensions = PegdownExtensions.NONE;
		List<Extension> flexMakkExtensions = new ArrayList<Extension>();
		String cleanExtensionName = null;
		boolean addExt = true;

		logger.info("Managing custom Markdown Extensions");

		for (String ext : mdExts) {
			if (ext.startsWith("-")) {
				cleanExtensionName = ext.substring(1);
				addExt = false;
			} else if (ext.startsWith("+")) {
				cleanExtensionName = ext.substring(1);
			} else {
				cleanExtensionName = ext;
			}

			int pegDownExtension = extensionFor(cleanExtensionName);

			if (pegDownExtension != PegdownExtensions.NONE) {
				if (addExt) {
					extensions = addExtension(extensions, pegDownExtension);
				} else {
					extensions = removeExtension(extensions, pegDownExtension);
				}
			} else {
				Extension flexMarkExt = searchFlexMarkExtension(cleanExtensionName);
				// store it, will be added *after* pegDown options added by adapter
				if (null != flexMarkExt) {
					flexMakkExtensions.add(flexMarkExt);
				} else {
					logger.warn("MarkDown extension '{}', NOT found", cleanExtensionName);
				}
			}
		}

		MutableDataSet allOptions = new MutableDataSet();

		DataHolder pegDownOptions = PegdownOptionsAdapter.flexmarkOptions(extensions);
		allOptions.setAll(pegDownOptions);

		Parser.addExtensions(allOptions, flexMakkExtensions.toArray(new Extension[0]));

		logger.info("Managing custom Markdown Options");
		// handle options
		if (null != mdOptions && !mdOptions.isEmpty()) {
			for (Map.Entry<String, String> entry : mdOptions.entrySet()) {
				String[] attributeDetails = entry.getKey().split("\\.");
				String classOwner;
				String fieldName;
				if (attributeDetails.length == 2) {
					classOwner = attributeDetails[0];
					fieldName = attributeDetails[1];
				} else {
					logger.info(
							"MarkDown option '{}', dosen't have a owner. ALL holder with that attribute will be modified. You may experience strange behaviour",
							attributeDetails.toString());
					classOwner = null;
					fieldName = attributeDetails[0];
				}
				String optionValue = entry.getValue();
				List<DataKey<?>> optionKeys = getDataValueSetter(classOwner, fieldName);

				for (DataKey<?> optionKey : optionKeys) {
					if (null == optionKey) {
						continue;
					}
					logger.info("Trying to set attribute '{}' with value '{}'", optionKey, optionValue);

					String optionDataType = optionKey.getDefaultValue().getClass().getName();

					switch (optionDataType) {
					case "java.lang.String":
						allOptions.set((DataKey<String>) optionKey, optionValue);
						break;
					case "java.lang.Boolean":
						allOptions.set((DataKey<Boolean>) optionKey, Boolean.valueOf(optionValue));
						break;
					case "java.lang.Integer":
						try {
							allOptions.set((DataKey<Integer>) optionKey, Integer.valueOf(optionValue));
						} catch (NumberFormatException nfe) {
							logger.warn("Value '{}' is not a valid Integer value for option {}", optionValue, optionKey);
						}
						break;
					case "com.vladsch.flexmark.util.sequence.BasedSequence":
						allOptions.set((DataKey<BasedSequence>) optionKey, BasedSequence.of(optionValue));
						break;
					case "java.util.Map":
					case "java.util.HashMap":
						Map<String, String> mapValue = convertOptionValueToMap(optionValue);
						if (null != mapValue) {
							allOptions.set((DataKey<Map>) optionKey, mapValue);
						}
						break;
					default:
						// check if an enum
						try {
							Class myEnum = Class.forName(optionDataType);
							if (myEnum.isEnum()) {
								processEnumOption(allOptions, myEnum, optionKey, optionValue);
								continue;
							}
						} catch (ClassNotFoundException e) {
							// Ignore
						}

						logger.warn("Cannot manage option of type : {} this option is ignored", optionDataType);
					}
				}
			}
		}

		logger.info("Markdown options : " + allOptions);

		Parser parser = Parser.builder(allOptions).build();
		HtmlRenderer renderer = HtmlRenderer.builder(allOptions).build();

		Document document = parser.parse(context.getBody());
		context.setBody(renderer.render(document));
	}

	private Extension searchFlexMarkExtension(String name) {
		Extension ext = null;
		for (Class<?> anExtension : flexMarkExtentionClasses) {
			if (anExtension.getSimpleName().equalsIgnoreCase(name + "Extension")) {
				Method builder;
				try {
					builder = anExtension.getDeclaredMethod("create");
					Object loadedExtention = builder.invoke(null);
					ext = (Extension) loadedExtention;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					logger.warn("Cannot instantiate extention '{}', this extention will be ignored", name);
				}
				break;
			}
		}
		return ext;
	}

	/**
	 * Class owning the attribute. Can be NULL but not recommended (all Owner with
	 * that atribut will be set with the value)
	 * 
	 * @param onwer     owning class of property to change (must extends
	 *                  com.vladsch.flexmark.IRender or
	 *                  com.vladsch.flexmark.IParser). Can be NULL.
	 * @param fieldName the name field to search for.
	 * @return a pointer to the current value.
	 */
	private List<DataKey<?>> getDataValueSetter(String onwer, String fieldName) {
		List<DataKey<?>> returnVal = new ArrayList<DataKey<?>>();
		for (Class<?> aClassOptionsHolder : flexMarkOptionsHolderClasses) {
			if (null == onwer || aClassOptionsHolder.getSimpleName().equalsIgnoreCase(onwer)) {
				returnVal.add(extractFieldValue(aClassOptionsHolder, fieldName));
				// Only Update the speicified Owner not other potential match
				if (aClassOptionsHolder.getSimpleName().equalsIgnoreCase(onwer)) {
					break;
				}
			}
		}

		for (Class<?> aClassRenderOptionsHolder : flexMarkOptionsIRenderHolderClasses) {
			if (null == onwer || aClassRenderOptionsHolder.getSimpleName().equalsIgnoreCase(onwer)) {
				returnVal.add(extractFieldValue(aClassRenderOptionsHolder, fieldName));
				// Only Update the speicified Owner not other potential match
				if (aClassRenderOptionsHolder.getSimpleName().equalsIgnoreCase(onwer)) {
					break;
				}
			}
		}

		for (Class<?> aClassParserOptionsHolder : flexMarkOptionsIParseHolderClasses) {
			if (null == onwer || aClassParserOptionsHolder.getSimpleName().equalsIgnoreCase(onwer)) {
				returnVal.add(extractFieldValue(aClassParserOptionsHolder, fieldName));
				// Only Update the speicified Owner not other potential match
				if (aClassParserOptionsHolder.getSimpleName().equalsIgnoreCase(onwer)) {
					break;
				}
			}
		}

		if (returnVal.isEmpty()) {
			logger.warn("No option(s) '{}.{}' found in extension Holders : {}", onwer, fieldName,
					flexMarkOptionsHolderClasses);
		}

		return returnVal;
	}

	private DataKey<?> extractFieldValue(Class<?> classHolder, String fieldName) {
		DataKey<?> returnVal = null;
		Field field;
		try {
			field = classHolder.getDeclaredField(fieldName);
			Object fieldValue = field.get(null);
			returnVal = (DataKey<?>) fieldValue;
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException
				| ClassCastException e) {
			logger.warn("Cannot instantiate option '{}.{}', this option will be ignored, cause '{}'", classHolder,
					fieldName, e);
		}

		return returnVal;
	}

	private <T> void processEnumOption(MutableDataSet allOptions, Class myEnum, DataKey<T> optionKey,
			String optionValue) {
		try {
			allOptions.set((DataKey<T>) optionKey, (@NotNull T) Enum.valueOf(myEnum, optionValue));
		} catch (IllegalArgumentException e) {
			logger.warn("Cannot manage option of type : {} value {} is not a valid value. Allowed values {}",
					myEnum.getName(), optionValue, myEnum.getEnumConstants());
		}
	}

	private Map<String, String> convertOptionValueToMap(String optionValue) {
		Map<String, String> returnVal = null;
		boolean hasError = false;
		StringTokenizer tokenizer = new StringTokenizer(optionValue, " ");

		while (tokenizer.hasMoreTokens()) {
			if (null == returnVal) {
				returnVal = new HashMap<String, String>();
			}
			String token = tokenizer.nextToken();
			String[] keyValue = token.split("=");
			try {
				returnVal.put(keyValue[0], keyValue[1]);
			} catch (ArrayIndexOutOfBoundsException e) {
				hasError = true;
				logger.warn("String value for map is not valid '{}'", token);
			}
		}

		if (returnVal.isEmpty() && hasError) {
			// avoid "reset" of default map if contain only invalid config)
			logger.warn("Map does not contain valid values. Using fallback instead.");
			returnVal = null;
		}

		return returnVal;
	}

	private int extensionFor(String name) {
		int extension = PegdownExtensions.NONE;

		try {
			Field extField = PegdownExtensions.class.getDeclaredField(name);
			extension = extField.getInt(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.debug(
					"Undeclared extension field '{}', for pegDown will try to search for standard FlexMark Extension",
					name);
		}
		return extension;
	}

	private int addExtension(int previousExtensions, int additionalExtension) {
		return previousExtensions | additionalExtension;
	}

	private int removeExtension(int previousExtensions, int unwantedExtension) {
		return previousExtensions & (~unwantedExtension);
	}

}
