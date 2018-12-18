package bsuir.ksis.angieapp;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.navigation.Navigation;
import bsuir.ksis.angieapp.interfaces.ISignInManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {

    ISignInManager signInManager;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        signInManager = (ISignInManager)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInFragmentDirections.ActionDestinationSignInToSignUpFragment action = SignInFragmentDirections.actionDestinationSignInToSignUpFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        getActivity().findViewById(R.id.signInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = ((EditText)getActivity().findViewById(R.id.loginEditText)).getText().toString();
                String password = ((EditText)getActivity().findViewById(R.id.passwordEditText)).getText().toString();
                boolean isSignedIn = signInManager.signIn(login, password);
                getActivity().findViewById(R.id.signInError).setVisibility(isSignedIn ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

}
