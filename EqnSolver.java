import java.util.*;
public class EqnSolver {
	public static char ACCEPTABLE_VAL[] = new char [] {'0','1','2','3','4','5','6','7','8','9'};
	public static char ACCEPTABLE_VAR[] = new char [] {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	public static char ACCEPTABLE_BINOP[] = new char [] {'+','-','*','/'};
	public static boolean contains(char [] acceptable, char symbol) {
		for (char c : acceptable) {
			if (symbol == c) {
				return true;
			}
		}
		return false;
	}
	public enum Type {
		VAL, VAR, BINOP
	}
	public static int precedence(char symbol) {
		switch (symbol) {
			case '+': return 0;
			case '-': return 0;
			case '*': return 1;
			case '/': return 1;
		}
		return -1;
	}
	public static class Token<T> {
		Type type;
		T data;
		public Token(Type type, T data) {
			this.type = type;
			this.data = data;
		}
		public String toString() {
			return "[" + data + "]"; 
		}
	}
	public enum LexMode {
		BINOP, VAR, VAL, NIL
	}
	public static Token tokenize(String accum, LexMode lexMode) {
		switch (lexMode) {
			case VAL: return new Token(Type.VAL, Integer.parseInt(accum));
			case VAR: return new Token(Type.VAR, accum);
			case BINOP: return new Token(Type.BINOP, accum);
		}
		return null;
	}
	public static Token [] lex(String input) {
		char[] chars = input.toCharArray();
		ArrayList<Token> tokens = new ArrayList<Token>();
		String accum = "";
		LexMode mode = LexMode.NIL;
		int i = 0;
		for (char c : chars) {
			if (contains(ACCEPTABLE_VAL, c)) {
				if (mode != LexMode.VAL && mode != LexMode.NIL) {
					tokens.add(tokenize(accum,mode));
					accum = "";
				}
				mode = LexMode.VAL;
				accum += c;
			}
			else if (contains(ACCEPTABLE_VAR, c)) {
				if (mode != LexMode.VAR && mode != LexMode.NIL) {
					tokens.add(tokenize(accum,mode));
					accum = "";
				}
				mode = LexMode.VAR;
				accum += c;
			}
			else if (contains(ACCEPTABLE_BINOP, c)) {
				if (mode != LexMode.BINOP && mode != LexMode.NIL) {
					tokens.add(tokenize(accum,mode));
					accum = "";
				}
				mode = LexMode.BINOP;
				accum += c;
			}
		}
		tokens.add(tokenize(accum,mode));
		return tokens.toArray(new Token[tokens.size()]);
	}
	public static class Ast<T> {
		T data;
		Type type;
		Ast [] children;
		Ast parent;
		public String toString() {
			return "" + data;
		}
	}
	public static Ast parse(Token [] tokens) {
		Ast cur = null;
		Ast tmp = null;
		for (Token t : tokens) {
			switch (t.type) {
			case VAL: case VAR:
				tmp = new Ast();
				tmp.data = t.data;
				tmp.type = t.type;
				tmp.children = null;
				tmp.parent = null;
				if (cur == null) {
					cur = tmp;
				}
				else if (cur.type == Type.BINOP) {
					cur.children[1] = tmp;
					tmp.parent = cur;
				}
				break;
			case BINOP:
				tmp = new Ast();
				tmp.data = t.data;
				tmp.type = t.type;
				tmp.children = new Ast[2];
				if (cur.type == Type.VAL) {
					cur.parent = tmp;
					tmp.children[0] = cur;
					cur = tmp;
				}
				else if (cur.type == Type.BINOP) {
					char curOp = ((String) cur.data).toCharArray()[0];
					char tmpOp = ((String) tmp.data).toCharArray()[0];
					if (precedence(curOp) == precedence(tmpOp) && cur.parent == null) {
						cur.parent = tmp;
						tmp.children[0] = cur;
						cur = tmp;
					} 
					else if (precedence(curOp) == precedence(tmpOp) && cur.parent != null) {
						tmp.parent = cur.parent;
						cur.parent.children[1] = tmp;
						cur.parent = tmp;
						tmp.children[0] = cur;
						cur = tmp;
					}
					else if (precedence(curOp) < precedence(tmpOp) && cur.parent == null) {
						cur.children[1].parent = tmp;
						tmp.children[0] = cur.children[1];
						tmp.parent = cur;
						cur.children[1] = tmp;
						cur = tmp;
					}
					else if (precedence(curOp) > precedence(tmpOp) && cur.parent == null) {
						tmp.children[0] = cur;
						cur.parent = tmp;
						cur = tmp;
					}
					else if (precedence(curOp) > precedence(tmpOp) && cur.parent != null) {
						tmp.children[0] = cur.parent;
						cur.parent.parent = tmp;
						cur = tmp;
					}
				}
				break;
			}
		}
		while (cur.parent != null) {
			cur = cur.parent;
		}
		return cur;
	}
	public static void simplify(Ast expression) {
		
	}
	public static Ast find(Ast expression, Ast target) {
		switch ((Type) expression.type) {
		case VAL: return null;
		case VAR: return expression.data.toString();
		case VAL: case VAR:
			return expression.data.toString();
		case BINOP: 
			return (print (expression.children[0])) + expression.data.toString() + (print (expression.children[1]));
		}
		return "";
	}
	public static String print(Ast expression) {
		switch ((Type) expression.type) {
		case VAL: case VAR:
			return expression.data.toString();
		case BINOP: 
			return (print (expression.children[0])) + expression.data.toString() + (print (expression.children[1]));
		}
		return "";
	}
	public static int simplify (Ast expression) {
		switch ((Type) expression.type) {
		case VAL: 
			return (int) expression.data;
		case BINOP: 
			switch ((String) expression.data) {
			case "+": return (simplify (expression.children[0])) + (simplify (expression.children[1]));
			case "-": return (simplify (expression.children[0])) - (simplify (expression.children[1]));
			case "*": return (simplify (expression.children[0])) * (simplify (expression.children[1]));
			case "/": return (simplify (expression.children[0])) / (simplify (expression.children[1]));
			}
		}
		return -1;
	}
	public static void main(String args[]) {
		Token [] lexed = lex(args[0]);
		Ast parsed = parse(lexed);
		String result = print(parsed);
		System.out.println(result);
	}
}