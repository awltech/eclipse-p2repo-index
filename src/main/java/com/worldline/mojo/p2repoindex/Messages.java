/**
 *
 */
package com.worldline.mojo.p2repoindex;

import java.util.Locale;
import java.util.ResourceBundle;

import java.text.MessageFormat;

/**
 * Enumeration containing internationalisation-related messages and API.
 *
 * @generated com.worldline.awltech.i18ntools.wizard
 */
public enum Messages {
	STARTING("STARTING"), STARTING_PARAM_PROJECT("STARTING_PARAM_PROJECT"), STARTING_PARAM_REPO("STARTING_PARAM_REPO"), STARTING_PARAM_DOC("STARTING_PARAM_DOC"), REPO_PATH_NOT_SPECIFIED("REPO_PATH_NOT_SPECIFIED"), REPO_PROJECT_FOUND("REPO_PROJECT_FOUND"), ERROR_ENCOUNTERED("ERROR_ENCOUNTERED"), REPO_FOLDER_FOUND("REPO_FOLDER_FOUND"), PROCESSING_REPOSITORY("PROCESSING_REPOSITORY"), ABORT_PATH_NULL("ABORT_PATH_NULL"), START_INDEX_GEN("START_INDEX_GEN"), START_STYLE_GEN("START_STYLE_GEN"), DONE_INDEX_GEN("DONE_INDEX_GEN"), ERROR_INDEX_GEN("ERROR_INDEX_GEN"), ERROR_STYLE_GEN("ERROR_STYLE_GEN"), DONE_STYLE_GEN("DONE_STYLE_GEN"), EXCEPTION_LOCATING_REPO("EXCEPTION_LOCATING_REPO"), CURRENT_PROJ_IS_REPO("CURRENT_PROJ_IS_REPO"), WARN_REPO_NOT_FOUND("WARN_REPO_NOT_FOUND"), LOCATING_DESCRIPTOR("LOCATING_DESCRIPTOR"), RESOLVED_DESCRIPTOR("RESOLVED_DESCRIPTOR"), RESOLVED_JAR_DESCRIPTOR("RESOLVED_JAR_DESCRIPTOR"), RESOLVED_JAR_FILE("RESOLVED_JAR_FILE"), DESCRIPTOR_NOT_RESOLVED("DESCRIPTOR_NOT_RESOLVED"), PROCESSING_DESCRIPTOR_CONTENTS("PROCESSING_DESCRIPTOR_CONTENTS"), CREATING_UNIT_CATEGORY("CREATING_UNIT_CATEGORY"), CREATING_UNIT_GROUP("CREATING_UNIT_GROUP"), CREATING_UNIT_FEATURE("CREATING_UNIT_FEATURE"), CREATED_CATEGORY("CREATING_CATEGORY"), FOUND_GROUP_MAPPING("FOUND_GROUP_MAPPING"), ADDED_FEATURE("ADDED_FEATURE"), FEATURE_ADDED_TO_GROUP("FEATURE_ADDED_TO_GROUP")
	;

	/*
	 * Value of the key
	 */
	private final String messageKey;

	/*
	 * Constant ResourceBundle instance
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Messages", Locale.getDefault());

	/**
	 * Private Enumeration Literal constructor
	 * 
	 * @param messageKey
	 *            value
	 */
	private Messages(final String messageKey) {
		this.messageKey = messageKey;
	}

	/**
	 * @return the message associated with the current value
	 */
	public String value() {
		if (Messages.RESOURCE_BUNDLE == null || !Messages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return Messages.RESOURCE_BUNDLE.getString(this.messageKey);
	}

	/**
	 * Formats and returns the message associated with the current value.
	 *
	 * @see java.text.MessageFormat
	 * @param parameters
	 *            to use during formatting phase
	 * @return formatted message
	 */
	public String value(final Object... args) {
		if (Messages.RESOURCE_BUNDLE == null || !Messages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return MessageFormat.format(Messages.RESOURCE_BUNDLE.getString(this.messageKey), args);
	}

}
