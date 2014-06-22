package org.eclipse.bpel.ui.widgets.provider;

import org.eclipse.swt.graphics.Color;

public class ExtendedStringArray {
  private String[] str = null;
  private Color color = null;

  public ExtendedStringArray(String[] str, Color color) {
    this.str = str;
    this.color = color;
  }

  public String[] getStringArray() {
    return str;
  }

  public Color getColor() {
    return color;
  }
}