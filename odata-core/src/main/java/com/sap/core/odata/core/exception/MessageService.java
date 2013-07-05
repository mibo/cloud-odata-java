/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.core.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sap.core.odata.api.exception.MessageReference;

/**
 * @author SAP AG
 */
public class MessageService {
  private static final String BUNDLE_NAME = "i18n";

  private final ResourceBundle resourceBundle;
  private final Locale requestedLocale;

  private static final Map<Locale, MessageService> LOCALE_2_MESSAGE_SERVICE = new HashMap<Locale, MessageService>();

  private MessageService(final Locale locale) {
    requestedLocale = locale;
    resourceBundle = createResourceBundle(locale);
  }

  /**
   * Create a {@link ResourceBundle} based on given locale and name ({@value #BUNDLE_NAME}).
   * If during creation an exception occurs it is catched and an special bundle is created with error type and message of
   * this exception.
   * 
   * @param locale for which locale the {@link ResourceBundle} is created
   * @return a {@link ResourceBundle}
   */
  private ResourceBundle createResourceBundle(final Locale locale) {
    ResourceBundle bundle;
    try {
      if (locale == null) {
        throw new IllegalArgumentException("Parameter locale MUST NOT be NULL.");
      }
      bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    } catch (final Exception e) {
      bundle = new ResourceBundle() {
        @Override
        protected Object handleGetObject(final String key) {
          return "MessageService could not be created because of exception '" +
              e.getClass().getSimpleName() + " with message '" + e.getMessage() + "'.";
        }

        @Override
        public Locale getLocale() {
          return Locale.ENGLISH;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Enumeration<String> getKeys() {
          return (Enumeration<String>) Collections.emptySet();
        }
      };
    }
    return bundle;
  }

  public static class Message {
    private final String localizedText;
    private final Locale locale;

    public Message(final Locale locale, final String localizedMessage) {
      super();
      localizedText = localizedMessage;
      this.locale = locale;
    }

    public String getText() {
      return localizedText;
    }

    public Locale getLocale() {
      return locale;
    }

    /**
     * Get language as defined in RFC 4646 based on {@link Locale}.
     * 
     * @return the language label
     */
    public String getLang() {
      Locale locale = getLocale();
      if (locale.getCountry().isEmpty()) {
        return locale.getLanguage();
      }
      return locale.getLanguage() + "-" + locale.getCountry();
    }
  }

  public static MessageService getInstance(final Locale locale) {
    MessageService messagesInstance = LOCALE_2_MESSAGE_SERVICE.get(locale);
    if (messagesInstance == null) {
      messagesInstance = new MessageService(locale);
      LOCALE_2_MESSAGE_SERVICE.put(locale, messagesInstance);
    }
    return messagesInstance;
  }

  public static boolean isLocaleSupported(final Locale locale) {
    if (locale == null) {
      return false;
    }
    MessageService service = getInstance(locale);
    return service.getLocale().equals(locale);
  }

  /**
   * Return first found supported {@link Locale} (iterating over list starting with first element).
   * If no {@link Locale} is supported <code>NULL</code> is returned.
   * 
   * @param locales
   * @return first supported {@link Locale} or <code>NULL</code>.
   */
  public static Locale getSupportedLocale(final List<Locale> locales) {
    return getSupportedLocale(locales, null);
  }

  /**
   * Return first found supported {@link Locale} (iterating over list starting with first element).
   * If no {@link Locale} is supported given <code>defaultLocale</code> is returned.
   * 
   * @param locales to be checked locales
   * @param defaultLocale local which is returned if no supported local is in given <code>locales</code> list
   * @return first supported {@link Locale} or given <code>defaultLocale</code>.
   */
  public static Locale getSupportedLocale(final List<Locale> locales, final Locale defaultLocale) {
    for (Locale locale : locales) {
      if (isLocaleSupported(locale)) {
        return locale;
      }
    }
    return defaultLocale;
  }

  public static MessageService getInstance(final List<Locale> locales) {
    MessageService service = null;

    for (Locale locale : locales) {
      service = getInstance(locale);
      if (service.getLocale().equals(locale)) {
        break;
      }
    }

    return service;
  }

  public static Message getMessage(final Locale language, final MessageReference context) {
    Object[] contentAsArray = context.getContent().toArray(new Object[0]);
    return getMessage(language, context.getKey(), contentAsArray);
  }

  public static Message getMessage(final Locale locale, final String key, final Object... replacements) {
    MessageService messages = MessageService.getInstance(locale);
    return messages.getMessage(key, replacements);
  }

  /**
   * 
   * @param key
   * @param replacements
   * @return the message
   */
  private Message getMessage(final String key, final Object... replacements) {
    String value = null;

    try {
      value = resourceBundle.getString(key);
      StringBuilder builder = new StringBuilder();
      Formatter f = new Formatter(builder, requestedLocale);
      f.format(value, replacements);
      f.close();

      return new Message(getLocale(), builder.toString());

    } catch (MissingResourceException e) {
      return new Message(Locale.ENGLISH, "Missing message for key '" + key + "'!");
    } catch (MissingFormatArgumentException e) {
      return new Message(Locale.ENGLISH, "Missing replacement for place holder in value '" + value +
          "' for following arguments '" + Arrays.toString(replacements) + "'!");
    }
  }

  public Locale getLocale() {
    return resourceBundle.getLocale();
  }

  public Enumeration<String> getKeys() {
    return resourceBundle.getKeys();
  }
}
