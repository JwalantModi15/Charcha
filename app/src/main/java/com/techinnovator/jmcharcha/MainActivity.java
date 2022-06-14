package com.techinnovator.jmcharcha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.techinnovator.jmcharcha.fragment.ChatFragment;
import com.techinnovator.jmcharcha.fragment.FindFragment;
import com.techinnovator.jmcharcha.fragment.RequestsFragment;
import com.techinnovator.jmcharcha.profile.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private FragmentAdapter fragmentAdapter;
    private boolean doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager2);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle());

        viewPager2.setAdapter(fragmentAdapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setCustomView(R.layout.tab_chat);
                        break;
                    case 1:
                        tab.setCustomView(R.layout.tab_request);
                        break;
                    case 2:
                        tab.setCustomView(R.layout.tab_find);
                        break;
                }
            }
        });

        tabLayoutMediator.attach();

        DatabaseReference databaseReferenceCurrUser = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());
        databaseReferenceCurrUser.child("Online").setValue(true);
        databaseReferenceCurrUser.child("Online").onDisconnect().setValue(false);
    }

    class FragmentAdapter extends FragmentStateAdapter{

        public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;

            switch (position){
                case 0:
                    fragment = new ChatFragment();
                    break;
                case 1:
                    fragment = new RequestsFragment();
                    break;
                case 2:
                    fragment = new FindFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onBackPressed() {

        if(tabLayout.getSelectedTabPosition()>0){
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }
        else{
            if(doubleBackPressed){
                finishAffinity();
            }
            else{
                doubleBackPressed = true;

                Toast.makeText(this,"Press back again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackPressed = false, 2100);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.changeProfile){
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}