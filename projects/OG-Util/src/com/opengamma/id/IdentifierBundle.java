/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable bundle of identifiers.
 * <p>
 * A bundle allows multiple identifiers to be used that all refer to the same conceptual object.
 * Each bundle will typically be in a different scheme.
 * <p>
 * For example, the United States might be referred to by an identifier referencing the 2 letter
 * ISO country code {@code US} in one identifier and the 3 letter currency code {@code USD} in another.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class IdentifierBundle implements Iterable<Identifier>, Serializable, Comparable<IdentifierBundle> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton empty bundle.
   */
  public static final IdentifierBundle EMPTY = new IdentifierBundle();
  /**
   * Fudge message key for the identifier set.
   */
  public static final String ID_FUDGE_FIELD_NAME = "ID";

  /**
   * The set of identifiers.
   */
  private final Set<Identifier> _identifiers;
  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;

  /**
   * Obtains an {@code IdentifierBundle} from an identifier.
   * 
   * @param identifier  the identifier to wrap in a bundle, not null
   * @return the identifier bundle, not null
   */
  public static IdentifierBundle of(Identifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    return new IdentifierBundle(ImmutableSet.of(identifier));
  }

  /**
   * Obtains an {@code IdentifierBundle} from an array of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null, no nulls in array
   * @return the identifier bundle, not null
   */
  public static IdentifierBundle of(Identifier... identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.noNulls(identifiers, "identifiers");
    return new IdentifierBundle(ImmutableSet.copyOf(identifiers));
  }

  /**
   * Obtains an {@code IdentifierBundle} from a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null, no nulls in array
   * @return the identifier bundle, not null
   */
  public static IdentifierBundle of(Collection<Identifier> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.noNulls(identifiers, "identifiers");
    return new IdentifierBundle(identifiers);
  }

  /**
   * Creates an empty bundle.
   */
  public IdentifierBundle() {
    _identifiers = Collections.emptySet();
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from a set of identifiers.
   * 
   * @param identifiers  the set of identifiers, assigned, not null
   */
  private IdentifierBundle(Collection<Identifier> identifiers) {
    _identifiers = ImmutableSet.copyOf(identifiers);
    _hashCode = calcHashCode();
  }

  /**
   * Recalculate the hash code on deserialization.
   * 
   * @param in  the input stream
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    _hashCode = calcHashCode();
  }

  /**
   * Calculate the hash code.
   * 
   * @return the hash code
   */
  private int calcHashCode() {
    return 31 + _identifiers.hashCode();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the collection of identifiers in the bundle.
   * 
   * @return the identifier collection, unmodifiable, not null
   */
  public Set<Identifier> getIdentifiers() {
    return _identifiers;
  }

  /**
   * Returns a new bundle with the specified identifier added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param identifier  the identifier to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public IdentifierBundle withIdentifier(Identifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    Set<Identifier> ids = new HashSet<Identifier>(_identifiers);
    ids.add(identifier);
    return new IdentifierBundle(ids);
  }

  /**
   * Returns a new bundle with the specified identifier removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param identifier  the identifier to remove from the returned bundle, null ignored
   * @return the new bundle, not null
   */
  public IdentifierBundle withoutIdentifier(Identifier identifier) {
    Set<Identifier> ids = new HashSet<Identifier>(_identifiers);
    ids.remove(identifier);
    return new IdentifierBundle(ids);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the standalone identifier for the specified scheme.
   * <p>
   * This returns the first identifier in the internal set that matches.
   * The set is not sorted, so this method is not consistent.
   * 
   * @param scheme  the scheme to query, null returns null
   * @return the standalone identifier, null if not found
   */
  public String getIdentifier(IdentificationScheme scheme) {
    for (Identifier identifier : _identifiers) {
      if (ObjectUtils.equals(scheme, identifier.getScheme())) {
        return identifier.getValue();
      }
    }
    return null;
  }

  /**
   * Returns a new bundle with all references to the specified scheme removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param scheme  the scheme to remove from the returned bundle, null ignored
   * @return the new bundle, not null
   */
  public IdentifierBundle withoutScheme(IdentificationScheme scheme) {
    Set<Identifier> ids = new HashSet<Identifier>(_identifiers.size());
    for (Identifier id : _identifiers) {
      if (id.isScheme(scheme) == false) {
        ids.add(id);
      }
    }
    return new IdentifierBundle(ids);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of identifiers in the bundle.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _identifiers.size();
  }

  /**
   * Returns an iterator over the identifiers in the bundle.
   * 
   * @return the identifiers in the bundle, not null
   */
  public Iterator<Identifier> iterator() {
    return _identifiers.iterator();
  }

  /**
   * Checks if this bundle contains all the keys from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns true, not null
   * @return true if this bundle contains all the keys from the specified bundle
   */
  public boolean containsAll(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (Identifier identifier : bundle.getIdentifiers()) {
      if (_identifiers.contains(identifier) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this bundle contains any key from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns false, not null
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean containsAny(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (Identifier identifier : bundle.getIdentifiers()) {
      if (_identifiers.contains(identifier)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this bundle contains the specified key.
   * 
   * @param identifier  the key to search for, null returns false
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean contains(Identifier identifier) {
    return identifier != null && _identifiers.contains(identifier);
  }

  /**
   * Converts this bundle to a list of formatted strings.
   * 
   * @return the list of identifiers as strings, not null
   */
  public List<String> toStringList() {
    List<String> list = new ArrayList<String>();
    for (Identifier id : this) {
      list.add(id.toString());
    }
    return list;
  }

  //-------------------------------------------------------------------
  @Override
  public int compareTo(IdentifierBundle other) {
    final Set<Identifier> mySet = getIdentifiers();
    final Set<Identifier> otherSet = other.getIdentifiers();
    if (mySet.size() < otherSet.size()) {
      return -1;
    }
    if (mySet.size() > otherSet.size()) {
      return 1;
    }
    final List<Identifier> myList = new ArrayList<Identifier>(mySet); // already sorted as TreeSet
    final List<Identifier> otherList = new ArrayList<Identifier>(otherSet); // already sorted as TreeSet
    for (int i = 0; i < myList.size(); i++) {
      int c = myList.get(i).compareTo(otherList.get(i));
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IdentifierBundle) {
      IdentifierBundle other = (IdentifierBundle) obj;
      return _identifiers.equals(other._identifiers);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder()
        .append("Bundle")
        .append("[")
        .appendWithSeparators(_identifiers, ", ")
        .append("]")
        .toString();
  }

  //-------------------------------------------------------------------------

  /**
   * Serializes this identifier bundle to a Fudge message. This is used by the Fudge Serialization Framework and Fudge-Proto generated
   * code to allow identifier bundles to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory a message creator, not {@code null}
   * @param message the message to serialize into, not {@code null}
   * @return the serialized message
   */
  public MutableFudgeMsg toFudgeMsg(final FudgeMsgFactory factory, final MutableFudgeMsg message) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(message, "message");
    for (Identifier identifier : getIdentifiers()) {
      message.add(ID_FUDGE_FIELD_NAME, identifier.toFudgeMsg(factory));
    }
    return message;
  }

  /**
   * Serializes this identifier bundle to a Fudge message. This is used by the Fudge Serialization Framework and Fudge-Proto generated
   * code to allow identifier bundles to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param factory a message creator, not {@code null}
   * @return the serialized Fudge message
   */
  public FudgeMsg toFudgeMsg(FudgeMsgFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes an identifier bundle from a Fudge message. This is used by the Fudge Serialization Framework and Fudge-Proto generated
   * code to allow identifier bundles to be embedded within Fudge-Proto specified messages with minimal overhead.
   * 
   * @param msg the Fudge message, not {@code null}
   * @return the identifier bundle
   */
  public static IdentifierBundle fromFudgeMsg(FudgeMsg msg) {
    Set<Identifier> identifiers = new HashSet<Identifier>();
    for (FudgeField field : msg.getAllByName(ID_FUDGE_FIELD_NAME)) {
      if (field.getValue() instanceof FudgeMsg == false) {
        throw new IllegalArgumentException("Message provider has field named " + ID_FUDGE_FIELD_NAME + " which doesn't contain a sub-Message");
      }
      identifiers.add(Identifier.fromFudgeMsg((FudgeMsg) field.getValue()));
    }
    return new IdentifierBundle(identifiers);
  }

}
