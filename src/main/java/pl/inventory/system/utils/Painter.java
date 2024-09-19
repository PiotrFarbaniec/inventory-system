package pl.inventory.system.utils;

public final class Painter {
  private static final String BLUE = "\\u001B[34m ";
  private static final String RED = "\\u001B[31m ";
  private static final String GREEN = "\\u001B[32m ";
  private static final String MAGENTA = "\\u001B[35m ";
  private static final String CYAN = "\\u001B[36m ";
  private static final String YELLOW = "\\u001B[33m ";
  private static final String WHITE = "\\u001B[37m ";
  private static final String RESET = " \\u001B[0m";

  private Painter() {
  }

  public static String paint(String content, Color color) {
    final String formatted;
    switch (color) {
      case BLUE -> formatted = BLUE + content + RESET;
      case RED -> formatted = RED + content + RESET;
      case GREEN -> formatted = GREEN + content + RESET;
      case MAGENTA -> formatted = MAGENTA + content + RESET;
      case CYAN -> formatted = CYAN + content + RESET;
      case YELLOW -> formatted = YELLOW + content + RESET;
      case WHITE -> formatted = WHITE + content + RESET;
      default -> formatted = content;
    }
    return formatted;
  }
}
