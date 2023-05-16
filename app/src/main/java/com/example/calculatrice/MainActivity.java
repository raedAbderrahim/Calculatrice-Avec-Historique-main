package com.example.calculatrice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    SQLiteDatabase db;
    TextView workingsTV;
    TextView resultsTV;
    TextView resultdbTextView;
    TextView calculationTextView;

    ListView  historyListView  ;
    String workings = "";
    String formula = "";
    String tempFormula = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTextViews();
        db = openOrCreateDatabase("DBCalculatice", MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS calcularice_history ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "calculation TEXT,"
                + "result TEXT)");
        displayLastHistoryEntry();


    }

    private void initTextViews()
    {

        calculationTextView = (TextView)findViewById(R.id.calculationTextView);
        resultdbTextView = (TextView)findViewById(R.id.resultdbTextView);
        historyListView = (ListView) findViewById(R.id.historyListView);
        workingsTV = (TextView)findViewById(R.id.workingsTextView);
        resultsTV = (TextView)findViewById(R.id.resultTextView);
    }

    private void setWorkings(String givenValue)
    {
        workings = workings + givenValue;
        workingsTV.setText(workings);
    }


    public void equalsOnClick(View view)
    {
        Double result = 0.0;
        try {
            if (workings.endsWith("+") || workings.endsWith("-") || workings.endsWith("*") || workings.endsWith("/")) {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                return;
            }
            if (workings.equals("+") || workings.equals("-") || workings.equals("*")|| workings.equals("*") ) {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                return;
            }

        if (workings != "" && workings != null){
            result = eval(workings);
            ContentValues values = new ContentValues();
            values.put("calculation", workings);
            values.put("result", result);
            db.insert("calcularice_history", null, values);
            displayLastHistoryEntry();
            if(result != null){
                resultsTV.setText(String.valueOf(result.doubleValue()));
                workings = "";
            }
        }

        } catch (Exception e)
        {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
        }
    }





    private boolean isNumeric(char c)
    {
        if((c <= '9' && c >= '0') || c == '.')
            return true;

        return false;
    }


    public void clearOnClick(View view)
    {
        workingsTV.setText("");
        workings = "";
        resultsTV.setText("");
        leftBracket = true;
    }

    boolean leftBracket = true;

    public void removeOnClick(View view)
    {

        String originalString = workingsTV.getText().toString();
        workings = originalString.substring(0, originalString.length() - 1);
        workingsTV.setText(workings);
    }


    public void divisionOnClick(View view)
    {
        if (workings == "" || workings == null) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }
        setWorkings("/");
    }

    public void sevenOnClick(View view)
    {
        setWorkings("7");
    }

    public void eightOnClick(View view)
    {
        setWorkings("8");
    }

    public void nineOnClick(View view)
    {
        setWorkings("9");
    }

    public void timesOnClick(View view)
    {
        if (workings == "" || workings == null) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }
        setWorkings("*");
    }

    public void fourOnClick(View view)
    {
        setWorkings("4");
    }

    public void fiveOnClick(View view)
    {
        setWorkings("5");
    }

    public void sixOnClick(View view)
    {
        setWorkings("6");
    }

    public void minusOnClick(View view)
    {
        if (workings == "" || workings == null) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }
        setWorkings("-");
    }

    public void oneOnClick(View view)
    {
        setWorkings("1");
    }

    public void twoOnClick(View view)
    {
        setWorkings("2");
    }

    public void threeOnClick(View view)
    {
        setWorkings("3");
    }

    public void plusOnClick(View view)
    {
        if (workings == "" || workings == null) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }
        setWorkings("+");
    }

    public void decimalOnClick(View view)
    {
        setWorkings(".");
    }

    public void zeroOnClick(View view)
    {
        setWorkings("0");
    }
    private void displayLastHistoryEntry() {

        Cursor cursor = db.rawQuery("SELECT id AS _id, calculation, result  FROM calcularice_history ORDER BY id asc", null);
        if (cursor.moveToFirst()) {

            HistoryCursorAdapter adapter = new HistoryCursorAdapter(this, cursor, 0);
            historyListView.setAdapter(adapter);
            historyListView.setSelection(cursor.getCount() - 1);

        }

    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected:  " + (char)ch);
                return x;
            }
            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            // | functionName `(` expression `)` | functionName factor
            // | factor `^` factor
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus
                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')'  after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x =
                            Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x =
                            Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x =
                            Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " +
                                func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                return x;
            }
        }.parse();
    }
}