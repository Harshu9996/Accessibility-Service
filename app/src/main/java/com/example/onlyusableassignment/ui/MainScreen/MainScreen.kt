package com.example.onlyusableassignment.ui.MainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.onlyusableassignment.R

@Composable
fun MainScreen(modifier: Modifier = Modifier,isServiceStarted : MutableState<Boolean>, click:()->Unit,stopServiceClick:()->Unit) {

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Button(onClick = {
            click()
        }) {
            Text(text = stringResource(id = R.string.allow_permission))
        }

    }

        if(isServiceStarted.value){
            Box(modifier = modifier, contentAlignment = Alignment.Center){
                Button(onClick = {
                   stopServiceClick()
                }) {
                    Text(text = stringResource(id = R.string.stop_service))
                }

            }
        }else{

        Text(text =  stringResource(R.string.allow_permission_text))
        }


    }


}

