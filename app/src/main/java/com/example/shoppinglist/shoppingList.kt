package com.example.shoppinglist

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

data class ShoppingItems (
    val id:Int,
    var name:String,
    var qty :Int,
    var isEditing: Boolean = false,
    var address : String = ""
    )



@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewmodel : LocationViewModel,
    navController: NavController,
    context : Context,
    address : String
    ) {
    var sitems by remember { mutableStateOf(listOf<ShoppingItems>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemQty by remember {mutableStateOf("")}


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions() ,
        onResult = {permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ){
                // I have access to location

                locationUtils.requestLocationUpdates(viewModel = viewmodel)
            }else{
                //Ask for permission
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )|| ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if(rationaleRequired){
                    Toast.makeText(context,
                        "Location Permission is required for this feature to work", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context,
                        "Location Permission is required ,Please enable it in the Android Settings",
                        Toast.LENGTH_LONG).show()

                }
            }
        }  )

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                showDialog = true
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Add Item")
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(sitems) {
                        item ->
                if(item.isEditing){
                    ShoppingItemEditor(item = item, onEditComplete = {
                        editedName,editedQuantity ->
                        sitems = sitems.map{it.copy(isEditing = false)}
                        val editedItem = sitems.find{it.id==item.id}
                        editedItem ?.let{
                            it.name = editedName
                            it.qty = editedQuantity
                            it.address = address
                        }
                    })
                }else{
                    ShoppingListItem(item = item, onEditClick = {
                        //finds out which item we are editing and changing its "isEditing boolean" to true
                        sitems = sitems.map{it.copy(isEditing = it.id==item.id)}
                    }, onDeleteClick = {
                        sitems = sitems - item
                    })
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            confirmButton = {
                Row(modifier =Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                    Button(onClick = {
                        if(itemName.isNotEmpty()){
                            val newItem = ShoppingItems(id = sitems.size +1,
                                name = itemName,
                                qty = itemQty.toInt(),
                                address = address
                            )
                             sitems += newItem
                            itemName = ""
                            itemQty = ""
                            showDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                    Button(onClick = {showDialog=false}) {
                        Text("Cancel")
                    }
                }

            },
            title = { Text("Add Shopping items") },
            text = {
                Column {
                   OutlinedTextField(
                       value = itemName,
                       onValueChange = {itemName = it},
                       label = { Text("Item Name") },
                       singleLine = true,
                       modifier = Modifier.fillMaxWidth().padding(8.dp)
                       )

                    OutlinedTextField(
                        value = itemQty,
                        onValueChange = {itemQty = it},
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        label = {Text("Quantity")}
                    )

                    Button(onClick ={
                        if(locationUtils.locationPermission(context)){
                            locationUtils.requestLocationUpdates(viewmodel)
                            navController.navigate("locationscreen"){
                                this.launchSingleTop
                            }
                        }else{
                            requestPermissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }

                    } ) {
                        Text("address")
                    }

                }//column ka hai ye
            }
        )
    }

}

@Composable
fun ShoppingListItem(
    item : ShoppingItems,
    onEditClick : () -> Unit,
    onDeleteClick : () -> Unit
){
  Row(modifier = Modifier.padding(8.dp).fillMaxSize().border(
      border = BorderStroke(2.dp, Color(0xFF018786)),
      shape = RoundedCornerShape(20)
  ),
      horizontalArrangement = Arrangement.SpaceBetween
      ) {
      Column(modifier = Modifier.fillMaxSize().weight(1f).padding(8.dp)) {
          Row {
              Text(text = item.name , modifier = Modifier.padding(8.dp))
              Text(text = " Qty ${item.qty}", modifier = Modifier.padding(8.dp))
          }
          Row(modifier = Modifier.fillMaxWidth()) {
              Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
              Text(text = item.address)
          }
      }

      Row(modifier = Modifier.padding(8.dp)) {
          IconButton(onClick = onEditClick) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = null)
          }
          IconButton(onClick = onDeleteClick) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = null)
          }
      }
  }
}

@Composable
fun ShoppingItemEditor(item:ShoppingItems,onEditComplete:(String,Int) -> Unit){
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.qty.toString()) }
    var isEditing by remember { mutableStateOf(item.isEditing) }

    Row (modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .border(
            border = BorderStroke(2.dp, Color(0xFF018786)),
            shape = RoundedCornerShape(20)
        )
        .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
        ){
          Column {
              BasicTextField(
                  value = editedName,
                  onValueChange = {editedName = it},
                  singleLine = true,
                  modifier = Modifier.wrapContentSize().padding(8.dp)
              )
              BasicTextField(
                  value = editedQuantity,
                  onValueChange = {editedQuantity = it},
                  singleLine = true,
                  modifier = Modifier.wrapContentSize().padding(8.dp)
              )
          }
        Button(
            onClick = {
                isEditing = false
                onEditComplete(editedName,editedQuantity.toIntOrNull() ?: 1)
            }
        ) {
            Text("Save")
        }

    }
}
