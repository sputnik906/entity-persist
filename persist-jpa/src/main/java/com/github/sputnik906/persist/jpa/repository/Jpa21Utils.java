package com.github.sputnik906.persist.jpa.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.AttributeNode;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Jpa21Utils {

  public static EntityGraph<?> tryGetFetchGraph(EntityManager em,String entityGraphName,String[] fetchProps,Class<?> entityType) {
    try {
      // first check whether an entityGraph with that name is already registered.
      return em.getEntityGraph(entityGraphName);
    } catch (Exception ex) {
      // try to create and dynamically register the entityGraph
      return createDynamicEntityGraph(em, fetchProps, entityType);
    }
  }

  private static EntityGraph<?> createDynamicEntityGraph(
    EntityManager em, String[] fetchProps, Class<?> entityType) {

    EntityGraph<?> entityGraph = em.createEntityGraph(entityType);

    List<String> attributePaths = new ArrayList<>(Arrays.asList(fetchProps));

    // Sort to ensure that the intermediate entity subgraphs are created accordingly.
    Collections.sort(attributePaths);

    for (String path : attributePaths) {

      String[] pathComponents = delimitedListToStringArray(path, ".");
      createGraph(pathComponents, 0, entityGraph, null);
    }

    return entityGraph;
  }

  private static void createGraph(String[] pathComponents, int offset, EntityGraph<?> root,
    Subgraph<?> parent) {

    String attributeName = pathComponents[offset];

    // we found our leaf property, now let's see if it already exists and add it if not
    if (pathComponents.length - 1 == offset) {

      if (parent == null && !exists(attributeName, root.getAttributeNodes())) {
        root.addAttributeNodes(attributeName);
      } else if (parent != null && !exists(attributeName, parent.getAttributeNodes())) {
        parent.addAttributeNodes(attributeName);
      }

      return;
    }

    AttributeNode<?> node = findAttributeNode(attributeName, root, parent);

    if (node != null) {

      Subgraph<?> subgraph = getSubgraph(node);

      if (subgraph == null) {
        subgraph = parent != null ? parent.addSubgraph(attributeName) : root.addSubgraph(attributeName);
      }

      createGraph(pathComponents, offset + 1, root, subgraph);

      return;
    }

    if (parent == null) {
      createGraph(pathComponents, offset + 1, root, root.addSubgraph(attributeName));
    } else {
      createGraph(pathComponents, offset + 1, root, parent.addSubgraph(attributeName));
    }
  }

  private static boolean exists(String attributeNodeName, List<AttributeNode<?>> nodes) {
    return findAttributeNode(attributeNodeName, nodes) != null;
  }

  private static  AttributeNode<?> findAttributeNode(String attributeNodeName, EntityGraph<?> entityGraph,
    Subgraph<?> parent) {
    return findAttributeNode(attributeNodeName,
      parent != null ? parent.getAttributeNodes() : entityGraph.getAttributeNodes());
  }

  private static AttributeNode<?> findAttributeNode(String attributeNodeName, List<AttributeNode<?>> nodes) {

    for (AttributeNode<?> node : nodes) {
      if (nullSafeEquals(node.getAttributeName(), attributeNodeName)) {
        return node;
      }
    }

    return null;
  }

  private static Subgraph<?> getSubgraph(AttributeNode<?> node) {
    return node.getSubgraphs().isEmpty() ? null : node.getSubgraphs().values().iterator().next();
  }

  private static boolean nullSafeEquals(Object o1,Object o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (o1.equals(o2)) {
      return true;
    }
    if (o1.getClass().isArray() && o2.getClass().isArray()) {
      return arrayEquals(o1, o2);
    }
    return false;
  }

  private static boolean arrayEquals(Object o1, Object o2) {
    if (o1 instanceof Object[] && o2 instanceof Object[]) {
      return Arrays.equals((Object[]) o1, (Object[]) o2);
    }
    if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
      return Arrays.equals((boolean[]) o1, (boolean[]) o2);
    }
    if (o1 instanceof byte[] && o2 instanceof byte[]) {
      return Arrays.equals((byte[]) o1, (byte[]) o2);
    }
    if (o1 instanceof char[] && o2 instanceof char[]) {
      return Arrays.equals((char[]) o1, (char[]) o2);
    }
    if (o1 instanceof double[] && o2 instanceof double[]) {
      return Arrays.equals((double[]) o1, (double[]) o2);
    }
    if (o1 instanceof float[] && o2 instanceof float[]) {
      return Arrays.equals((float[]) o1, (float[]) o2);
    }
    if (o1 instanceof int[] && o2 instanceof int[]) {
      return Arrays.equals((int[]) o1, (int[]) o2);
    }
    if (o1 instanceof long[] && o2 instanceof long[]) {
      return Arrays.equals((long[]) o1, (long[]) o2);
    }
    if (o1 instanceof short[] && o2 instanceof short[]) {
      return Arrays.equals((short[]) o1, (short[]) o2);
    }
    return false;
  }

  private static String[] delimitedListToStringArray(String str,String delimiter) {
    return delimitedListToStringArray(str, delimiter, null);
  }

  private static String[] delimitedListToStringArray(
    String str, String delimiter, String charsToDelete) {

    if (str == null) {
      return new String[0];
    }
    if (delimiter == null) {
      return new String[] {str};
    }

    List<String> result = new ArrayList<>();
    if (delimiter.isEmpty()) {
      for (int i = 0; i < str.length(); i++) {
        result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
      }
    }
    else {
      int pos = 0;
      int delPos;
      while ((delPos = str.indexOf(delimiter, pos)) != -1) {
        result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
        pos = delPos + delimiter.length();
      }
      if (str.length() > 0 && pos <= str.length()) {
        // Add rest of String, but not in case of empty input.
        result.add(deleteAny(str.substring(pos), charsToDelete));
      }
    }
    return result.toArray(new String[0]);
  }

  private static String deleteAny(String inString, String charsToDelete) {
    if (!hasLength(inString) || !hasLength(charsToDelete)) {
      return inString;
    }

    StringBuilder sb = new StringBuilder(inString.length());
    for (int i = 0; i < inString.length(); i++) {
      char c = inString.charAt(i);
      if (charsToDelete.indexOf(c) == -1) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static boolean hasLength(String str) {
    return (str != null && !str.isEmpty());
  }
}
