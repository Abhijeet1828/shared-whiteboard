package com.distributed.project.whiteboard.client.utilities;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * This class is used to convert objects to different structures using Jackson
 * {@link ObjectMapper}.
 * 
 * @author Abhijeet - 1278218
 *
 */
public final class TypeConversionUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeConversionUtils.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	private TypeConversionUtils() {
		throw new IllegalStateException("TypeConversionUtils class cannot be instantiated");
	}

	/**
	 * This method is used to convert Object to Custom Class provided in the
	 * parameters.
	 * 
	 * @param data
	 * @param clazz
	 * @return
	 */
	public static <T> T convertToCustomClass(Object data, Class<T> clazz) {
		try {
			if (Objects.nonNull(data) && Objects.nonNull(clazz)) {
				if (data instanceof String) {
					return OBJECT_MAPPER.readValue((String) data, clazz);
				} else {
					return OBJECT_MAPPER.convertValue(data, clazz);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in convertToCustomClass", e);
		}
		return null;
	}

	/**
	 * This method is used to convert Object to Map of String and Object.
	 * 
	 * @param data
	 * @return
	 */
	public static Map<String, Object> convertToMap(Object data) {
		try {
			if (Objects.nonNull(data)) {
				JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class,
						Object.class);
				if (data instanceof String) {
					return OBJECT_MAPPER.readValue((String) data, javaType);
				} else {
					return OBJECT_MAPPER.convertValue(data, javaType);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in convertToMap", e);
		}
		return Collections.emptyMap();
	}

	/**
	 * This method is used to convert given non-null object to String.
	 * 
	 * @param data
	 * @return
	 */
	public static String convertObjectToString(Object data) {
		try {
			if (Objects.nonNull(data)) {
				return OBJECT_MAPPER.writeValueAsString(data);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in convertObjectToString", e);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * This method is used to convert given object to Map<String, String>.
	 * 
	 * @param data
	 * @return
	 */
	public static Map<String, String> convertToStringkeyValueMap(Object data) {
		try {
			if (Objects.nonNull(data)) {
				JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class,
						String.class);
				if (data instanceof String) {
					return OBJECT_MAPPER.readValue((String) data, javaType);
				} else {
					return OBJECT_MAPPER.convertValue(data, javaType);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in convertToStringkeyValueMap", e);
		}
		return Collections.emptyMap();
	}

}
