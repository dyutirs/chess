package org.studyeasy.chess.model;

public record Position(int row, int col) {
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    public String toChessNotation() {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }
    
    public static Position fromChessNotation(String notation) {
        if (notation.length() != 2) {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        
        char file = notation.charAt(0);
        int rank = Character.getNumericValue(notation.charAt(1));
        
        int col = file - 'a';
        int row = 8 - rank;
        
        return new Position(row, col);
    }
}
