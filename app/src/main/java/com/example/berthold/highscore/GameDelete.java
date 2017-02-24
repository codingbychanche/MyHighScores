/**
 * Deletes the selected game and all of it's scores
 *
 * @author Berthold Fritz 2016
 */

package com.example.berthold.highscore;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GameDelete extends AppCompatActivity {

    private ListView myListView;
    private ArrayAdapter myListAdapter;
    private boolean yesNoResult;  // Yes no dialog result
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_delete);

        // Confirm Dialog

        DialogInterface.OnClickListener dialogClickListener=new DialogInterface.OnClickListener(){
            @Override
            public void onClick (DialogInterface dialog,int which){
                if(which==DialogInterface.BUTTON_POSITIVE) dialogReturn(true);
                if(which==DialogInterface.BUTTON_NEGATIVE) dialogReturn(false);
            }
        };

        // Build Dialog

        final AlertDialog.Builder chooseYesNo=new AlertDialog.Builder(this);
        chooseYesNo.setMessage(R.string.yesNo).setPositiveButton(R.string.yes,dialogClickListener).
                setNegativeButton(R.string.no,dialogClickListener);

        // Get data

        StringBuffer result=DB.sqlRequest("select name from games order by name", MainActivity.conn);
        String [] rs=result.toString().split("#");

        // Show List

        myListView=(ListView)findViewById(R.id.death_game_list);
        myListAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,rs);
        myListView.setAdapter(myListAdapter);

        // Querry user, when list item was clicked, delete this item....

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               // Show dialog and proceed deleting if dialog "yes" was clicked

                chooseYesNo.show();

                // When clicked get field 'titel'

                name = ((TextView) view).getText().toString();

            }
        });
    }

    /**
     * Callback for yesNoDialog.
     *
     * Deletes the selected DB entry if positive button of yesNoDialog was
     * clicked.
     *
     * @global  name    Game name from clicked List item
     * @param   confirmed true if positive button was clicked.
     */

    public void dialogReturn(boolean confirmed){

        yesNoResult=confirmed;

        if (confirmed) {
            int key1 = DB.getKey1("games", "name", name, MainActivity.conn);

            try {
                Statement s = MainActivity.conn.createStatement();
                // delete game
                s.executeUpdate("delete from games where key1=" + key1);
                // And all of it's scores...
                s.executeUpdate("delete from scores where key2=" + key1);

                // Restart activity (and show updated list of games to allow another one
                // to ew deleted or to go back to main
                Intent i=new Intent(GameDelete.this,GameDelete.class);
                startActivity(i);
                finish();

            } catch (SQLException e) {
                // TODO: 11/8/16  Give feedback
            }
        }
    }
}
