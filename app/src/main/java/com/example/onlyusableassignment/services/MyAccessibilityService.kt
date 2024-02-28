package com.example.onlyusableassignment.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ProgressBar
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.example.onlyusableassignment.R

class MyAccessibilityService : AccessibilityService() {
    val TAG = "MyAccessibilityService"
    //creating and initializing WindowManager instance
    private val windowManager:WindowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    private val nodesToDraw: HashSet<AccessibilityNodeInfo> = HashSet()
    private val newRectangleView: MutableList<View> = mutableListOf()
    private val rectangleViews:MutableList<View> = mutableListOf()

    private var progressBar: ProgressBar? = null
    //Handler for delayed tasks
    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var sharedPref : SharedPreferences


    override fun onCreate() {
        sharedPref = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)

        super.onCreate()
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        Log.d(TAG, "onAccessibilityEvent: event = $event")

        //Checking if the service was enabled
        val isServiceEnabled = sharedPref.getBoolean(getString(R.string.isServiceEnabled),false)

        if(isServiceEnabled && event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){

            //Show loading progress bar
            showProgressBar()

            //Post delayed task to handler
            handler.postDelayed({
                val root = rootInActiveWindow
                if(root==null){
                    hideProgressBar()
//                    Toast.makeText(this,"Please Change the Screen",Toast.LENGTH_SHORT).show()
                }

                Log.d(TAG, "onAccessibilityEvent: root = $root")

                root?.let {


                    //Clear any existing rectangles
                    clearExistingRect()

                    //Traverse the Accessibility tree to extract all nodes and save it to newNode set
                    traverseAccessibilityTree(it,0)

                    //Add new Rectangles
                    addNewRect()

                    //drawRectangles
                    drawRectangles()

                    //Hide Progress Bar
                    hideProgressBar()


                }
            },2000)
        }



    }

    override fun onInterrupt() {
        //Handle Interruption
        Log.d(TAG, "onInterrupt: MyAccessibilityService is interrupted")
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Accessibility service is destroyed")
    }
    override fun onServiceConnected() {
        super.onServiceConnected()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled),true).apply()
                showProgressBar()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun traverseAccessibilityTree(nodeInfo: AccessibilityNodeInfo,depth:Int){
        //Base case 1
        if(depth>=15){
            //Add current node
//            nodesToDraw.add(nodeInfo)
            Log.d(TAG, "traverseAccessibility: Reached to base case")
            return
        }
        //Base Case 2
        if(nodeInfo.childCount==0){
            nodesToDraw.add(nodeInfo)
            Log.d(TAG, "traverseAccessibility: Reached to base case")
            return
        }

        //Add current code
        nodesToDraw.add(nodeInfo)

        //Add all children Nodes
        for(i in 0 until nodeInfo.childCount){
            val child = nodeInfo.getChild(i)
            child?.let{
                traverseAccessibilityTree(child,depth + 1)
            }
        }
    }

    private fun addNewRect(){
        //Draw rectangle around nodes in nodesToDraw
        for(node in nodesToDraw){
            drawRect(node, newRectangleView)
        }

        //clear nodesToDraw list
        nodesToDraw.clear()

        handler.postDelayed({
            //Remove previously drawn rectangles from the screen
            clearExistingRect()

            //set service enable status to false
            sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled), false).apply()

        },3000)

    }

    private fun drawRectangles(){
        rectangleViews.addAll(newRectangleView)
        newRectangleView.clear()
    }

    private fun drawRect(nodeInfo:AccessibilityNodeInfo,targetList:MutableList<View>){
        //Retrieve bounds to draw node on screen
        val rect = Rect()
        nodeInfo.getBoundsInScreen(rect)

        //Get Root Node
        val root = rootInActiveWindow?:run{
            //root is null
            Log.d(TAG, "drawRect: Root is null")
            return
        }

        //calculate the offset based on root's position
        val offsetX = Rect().apply{root.getBoundsInScreen(this)}.left
        val offsetY = Rect().apply { root.getBoundsInScreen(this) }.top

        //Adjusting rectangle position
        rect.offset(-offsetX,-offsetY-80)
        
        //create view
        val rectView = View(this)
        
        //Set rectangle colour based on the clickability
        if(nodeInfo.isClickable){
            rectView.setBackgroundResource(R.drawable.bordered_rectangle_green)
            
        }else{
            rectView.setBackgroundResource(R.drawable.bordered_rectangle_red)
        }
        
        //Parameters for rectangular view
        val params = WindowManager.LayoutParams(
            rect.width(),
            rect.height()-25,
            rect.left,
            rect.top - 25,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        //Set gravity for rectangle
        params.gravity = Gravity.START or Gravity.TOP
        
        //Add view to windowManager and targetList
        windowManager.addView(rectView,params)
        targetList.add(rectView)

        Log.d(TAG, "drawRect: Rectangle drawn on screen at location ${rect.left}, ${rect.top} with size ${rect.width()} x ${rect.height()}")
    }

    private fun clearExistingRect(){
        //Remove existing rectangles
        for(view in rectangleViews){
            windowManager.removeView(view)
        }
        rectangleViews.clear()

        sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled),false).apply()
        hideProgressBar()

    }

    private fun showProgressBar(){
        if(progressBar==null){
            //initialize progressbar
            progressBar = ProgressBar(this)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            //Set Gravity
            params.gravity = Gravity.CENTER

            //Add progressBar to window
            windowManager.addView(progressBar,params)
        }
    }

    private fun hideProgressBar(){
        progressBar?.let {
            windowManager.removeView(it)
            progressBar = null
        }
    }
}