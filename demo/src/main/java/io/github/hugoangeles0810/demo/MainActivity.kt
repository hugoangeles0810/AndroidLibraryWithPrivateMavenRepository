package io.github.hugoangeles0810.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.hugoangeles0810.library.DummyLibraryClass
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDescription.text = DummyLibraryClass.getDescription()
    }
}
