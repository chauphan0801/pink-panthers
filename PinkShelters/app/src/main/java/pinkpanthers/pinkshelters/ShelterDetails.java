package pinkpanthers.pinkshelters;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShelterDetails extends AppCompatActivity {
    private DBI db;
    private Shelter s;
    private Homeless a; //current user that is logged in
    private TextView errorMessage;
    private TextView vacancy;
    private Shelter reservedShelter;
    private Button claimBedButton;
    private Button updateInfoButton;
    private Button cancelBedButton;
    private String username;
    private String message; //message for error message

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelter_details);
        db = new Db("pinkpanther", "PinkPantherReturns!", "pinkpanther");
        errorMessage = findViewById(R.id.errorMessage);
        vacancy = findViewById(R.id.vacancy);
        claimBedButton = findViewById(R.id.claimBed);
        updateInfoButton = findViewById(R.id.updateAccountButton);
        cancelBedButton = findViewById(R.id.cancelReservation);

        try {
            int shelterId = getIntent().getExtras().getInt("shelterId");
            s = db.getShelterById(shelterId);
            updateView(s);
        } catch (NoSuchUserException e) {
            throw new RuntimeException("This is not how it works " + e.toString());
        } catch (NullPointerException e) {
            throw new RuntimeException("NullPointerException is raised: getExtras() returns null in ListOfShelter");
        }

        try {
            username = getIntent().getExtras().getString("username");
            Account user = db.getAccountByUsername(username);
            if (user instanceof Homeless) { // user is a homeless person
                a = (Homeless) user;
                claimBedButton.setVisibility(View.VISIBLE);
                try {
                    reservedShelter = db.getShelterById(a.getShelterId());
                    if (reservedShelter.getId() == s.getId()) {
                        cancelBedButton.setVisibility(View.VISIBLE);
                        message = "You have claimed " + a.getFamilyMemberNumber()
                                + " bed(s) at this shelter";
                        errorMessage.setText(message);
                        errorMessage.setTextColor(Color.GREEN);
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                } catch (NoSuchUserException e) {
                    // this shelter is not the one that the user reserved to
                    // so nothing happens (cancel reservation button remains invisible)
                }
            } else { // user is not a homeless person
                claimBedButton.setVisibility(View.INVISIBLE);
            }
        } catch (NoSuchUserException e) {
            throw new RuntimeException("There is no user with that username or shelter with that ID");
        } catch (NullPointerException e) {
            throw new RuntimeException("getExtras() returns null username");
        }
    }

    private void updateView(Shelter s) {
        TextView name = findViewById(R.id.name);
        String forName = "Name: " + s.getShelterName();
        name.setText(forName);

        TextView capacity = findViewById(R.id.capacity);
        String forCapacity = "Capacity: " + s.getCapacity();
        capacity.setText(forCapacity);

        TextView longitude = findViewById(R.id.longitude);
        String forLongitude = "Longitude: " + s.getLongitude();
        longitude.setText(forLongitude);

        TextView latitude = findViewById(R.id.latitude);
        String forLatitude = "Latitude: " + s.getLatitude();
        latitude.setText(forLatitude);

        TextView restrictions = findViewById(R.id.restrictions);
        String forRestrictions = "Restrictions: " + s.getRestrictions();
        restrictions.setText(forRestrictions);

        TextView address = findViewById(R.id.address);
        String forAddress = "Address: " + s.getAddress();
        address.setText(forAddress);

        TextView phoneNum = findViewById(R.id.phoneNum);
        String forPhoneNum = "Phone Number: " + s.getPhoneNumber();
        phoneNum.setText(forPhoneNum);

        TextView specialNote = findViewById(R.id.specialNote);
        String forSpecialNote = "Special Note: " + s.getSpecialNotes();
        specialNote.setText(forSpecialNote);

        String forVacancy = "Vacancy: " + s.getVacancy();
        vacancy.setText(forVacancy);
    }

    public void claimBedButton(View view) {
        // check to see if user has updated their information
        if (a.getFamilyMemberNumber() == 0 || a.getRestrictionsMatch() == null) {
            message = "You need to update your information before you can claim a bed or beds. "
                    + "Please update your information by using the button below";
            errorMessage.setText(message);
            errorMessage.setVisibility(View.VISIBLE);
            updateInfoButton.setVisibility(View.VISIBLE);
        } else { //homeless person cant claim bed(s) if they have already claimed bed(s) at a different shelter
            int familyMemberNumber = a.getFamilyMemberNumber();
            if (a.getShelterId() != 0) {
                if (s.getId() == reservedShelter.getId()) {
                    message = "Sorry, you have already claimed " + familyMemberNumber +
                            " bed(s)";
                    errorMessage.setText(message);
                    errorMessage.setTextColor(Color.RED);
                } else {
                    message = "You have already claimed " + familyMemberNumber +
                            " bed(s) at another shelter called " + reservedShelter.getShelterName();
                    errorMessage.setText(message);
                    errorMessage.setTextColor(Color.RED);
                }
                errorMessage.setVisibility(View.VISIBLE);

                // check if there are still available bed(s) for their families
            } else if (s.getVacancy() <= familyMemberNumber) {
                message = "Sorry, there are not enough beds";
                errorMessage.setText(message);
                errorMessage.setVisibility(View.VISIBLE);

            } else {
                //set of restriction of shelter
                Set <String> shelterRestrictionSet = new HashSet <> ();

                //turn String of shelter restriction to List
                List <String> shelterRestrictionList = Arrays.asList(s.getRestrictions().split((", ")));

                //add Strings of shelter restriction list to set
                for (String s: shelterRestrictionList) {
                    shelterRestrictionSet.add(s.toLowerCase());
                }

                //list of homeless restriction
                List <String> homelessRestrictions = a.getRestrictionsMatch();

                //turn list into set of homeless restriction
                Set <String> homelessRestrictionsSet = new HashSet <> (homelessRestrictions);
                Set <String> common = new HashSet <> (shelterRestrictionSet);
                common.retainAll(homelessRestrictionsSet);
                String anyone1 = s.getRestrictions().toLowerCase().toString();
                Log.d("ahahahahaha", anyone1);
                if (anyone1.equals(new String("anyone ")) || (common.equals(homelessRestrictionsSet))) {
                    try {
                        //update vacancy of shelter
                        int vacancy1 = s.getVacancy() - familyMemberNumber;
                        s.setVacancy(vacancy1);

                        //update current vacancy on screen
                        String forVacancy = "Vacancy: " + s.getVacancy();
                        vacancy.setText(forVacancy);

                        //update shelter occupancy for Db
                        int occupancy = s.getUpdate_capacity() - vacancy1;
                        s.setOccupancy(occupancy);
                        db.updateShelterOccupancy(s.getId(), occupancy);
                        a.setShelterId(s.getId());

                        //pass in account object to update account
                        db.updateAccount(a);

                        // refresh page to update vacancy TextView
                        finish();
                        startActivity(getIntent());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NoSuchUserException e) {
                        e.printStackTrace();
                    }
                } else {
                    message = "You do not fit the restrictions of this shelter";
                    errorMessage.setText(message);
                    errorMessage.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    public void updateInfoButton(View view) {
        Intent updateInfoPage = new Intent(ShelterDetails.this, UserInfoActivity.class);
        updateInfoPage.putExtra("username", username);
        startActivity(updateInfoPage);
    }

    public void cancelReservationButton(View view) {
        // update vacany
        int vacancy1 = s.getVacancy() + a.getFamilyMemberNumber();
        s.setVacancy(vacancy1);
        String forVacancy = "Vacancy: " + vacancy1;
        vacancy.setText(forVacancy);

        //update occupancy
        int occupancy = s.getUpdate_capacity() - vacancy1;
        s.setOccupancy(occupancy);

        //update homeless's shelter id
        a.setShelterId(0); // no longer associate with any shelter
        try {
            db.updateAccount(a);
            db.updateShelterOccupancy(s.getId(), occupancy);
        } catch (NoSuchUserException e) {
            throw new RuntimeException("Homeless user is null or shelterid does not exist");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("SQLException raised when trying to update account or shelter" +
                    " during canceling reservation");
        }

        message = "You have successfully cancel your reservation for " +
                a.getFamilyMemberNumber() + " bed(s)";
        errorMessage.setText(message);
        errorMessage.setTextColor(Color.GREEN);
        errorMessage.setVisibility(View.VISIBLE);

        // refresh page to update vacancy textView
        finish();
        startActivity(getIntent());
    }

}