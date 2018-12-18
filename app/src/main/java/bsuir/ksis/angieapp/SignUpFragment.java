package bsuir.ksis.angieapp;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.navigation.Navigation;
import bsuir.ksis.angieapp.interfaces.ISignUpManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    ISignUpManager signUpManager;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        signUpManager = (ISignUpManager)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().findViewById(R.id.registerButtonUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = ((EditText)getActivity().findViewById(R.id.loginEditText)).getText().toString();
                String password = ((EditText)getActivity().findViewById(R.id.passwordEditText)).getText().toString();
                String confirmPassword = ((EditText)getActivity().findViewById(R.id.confirmPasswordEditText)).getText().toString();

                if (password.equals(confirmPassword)) {
                    boolean isRegistered = signUpManager.register(login, password);
                    if (isRegistered) {
                        getActivity().findViewById(R.id.registerError).setVisibility(View.INVISIBLE);
                    } else {
                        ((TextView)getActivity().findViewById(R.id.registerError)).setText(R.string.wrong_password);
                        getActivity().findViewById(R.id.registerError).setVisibility(View.VISIBLE);
                    }
                }
                else {
                    ((TextView)getActivity().findViewById(R.id.registerError)).setText(R.string.wrong_password);
                    getActivity().findViewById(R.id.registerError).setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
