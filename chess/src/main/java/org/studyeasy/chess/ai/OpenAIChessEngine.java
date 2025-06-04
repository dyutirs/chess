package org.studyeasy.chess.ai;

import  com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.studyeasy.chess.model.Board;
import org.studyeasy.chess.model.Position;
import org.studyeasy.chess.model.Piece;
import org.studyeasy.chess.model.PieceColor;

import java.util.ArrayList;
import java.util.List;

public class OpenAIChessEngine {
    private final OpenAiService service;
    
    public OpenAIChessEngine(String apiKey) {
        this.service = new OpenAiService(apiKey);
    }
    
    public Position[] getNextMove(Board board, List<String> moveHistory) {
        try {
            String boardState = convertBoardToFEN(board);
            String prompt = createPrompt(boardState, moveHistory);
            
            System.out.println("Sending prompt to OpenAI: " + prompt);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a chess engine. Analyze the board and suggest the best move for black. Respond ONLY with the move in format 'e7e5' (from square to square)."));
            messages.add(new ChatMessage("user", prompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o")
                    .messages(messages)
                    .temperature(0.2) // Lower temperature for more deterministic responses
                    .maxTokens(10) // We only need a short response
                    .build();
            
            String response = service.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
            System.out.println("OpenAI response: " + response);
            
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("Error getting AI move: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String convertBoardToFEN(Board board) {
        StringBuilder fen = new StringBuilder();
        
        // Board representation
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    
                    char pieceChar = getPieceChar(piece);
                    fen.append(pieceChar);
                }
            }
            
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            
            if (row < 7) {
                fen.append('/');
            }
        }
        
        // Add active color (always black for AI's turn)
        fen.append(" b");
        
        // Add castling availability (simplified)
        fen.append(" KQkq");
        
        // Add en passant target square (simplified)
        fen.append(" -");
        
        // Add halfmove clock and fullmove number (simplified)
        fen.append(" 0 1");
        
        return fen.toString();
    }

    private char getPieceChar(Piece piece) {
        char c = switch (piece.getType()) {
            case PAWN -> 'p';
            case ROOK -> 'r';
            case KNIGHT -> 'n';
            case BISHOP -> 'b';
            case QUEEN -> 'q';
            case KING -> 'k';
        };
        
        // Uppercase for white pieces
        if (piece.getColor() == PieceColor.WHITE) {
            c = Character.toUpperCase(c);
        }
        
        return c;
    }
    
    private String createPrompt(String boardState, List<String> moveHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Current board state in FEN: ").append(boardState).append("\n\n");
        
        if (!moveHistory.isEmpty()) {
            prompt.append("Move history: ");
            for (String move : moveHistory) {
                prompt.append(move).append(" ");
            }
            prompt.append("\n\n");
        }
        
        prompt.append("Provide the best move for black in the format 'e7e5' (from square to square). Respond ONLY with the move, no explanations.");
        return prompt.toString();
    }
    
    private Position[] parseResponse(String response) {
        try {
            // Extract move in format "e7e5"
            String move = response.replaceAll("[^a-h1-8]", "");
            System.out.println("Extracted move: " + move);
            
            if (move.length() >= 4) {
                move = move.substring(0, 4);
                Position from = Position.fromChessNotation(move.substring(0, 2));
                Position to = Position.fromChessNotation(move.substring(2, 4));
                System.out.println("Parsed positions: from " + from.row() + "," + from.col() + 
                                  " to " + to.row() + "," + to.col());
                return new Position[]{from, to};
            } else {
                System.err.println("Move string too short: " + move);
            }
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Using fallback move e7e5");
        // Fallback to a default move (e7 to e5)
        return new Position[]{
            Position.fromChessNotation("e7"),
            Position.fromChessNotation("e5")
        };
    }
}