package com.android.deliveryapp

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivitySsiWalletBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class SsiWallet : AppCompatActivity() {

    private lateinit var binding: ActivitySsiWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivitySsiWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.createssibutton.setOnClickListener {
            binding.animatedProgressBar.visibility= View.VISIBLE
            binding.createssibutton.visibility = View.INVISIBLE
            Toast.makeText(baseContext,"Creating SSI ",Toast.LENGTH_SHORT).show()
            val stringRequest: StringRequest = object : StringRequest( Method.POST, "https://core.ssikit.walt.id/v1/key/gen",
                Response.Listener { response ->


                    try {
                        extractResponse(response)
                        Toast.makeText(baseContext,"Successfully Generated Key ${response}",Toast.LENGTH_SHORT).show()
                        binding.animatedProgressBar.setProgress(25)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(baseContext, error.toString(), Toast.LENGTH_LONG).show()
                }) {
                override fun getBody(): ByteArray {
                    val params2 = HashMap<String, String>()
                    params2.put("keyAlgorithm","EdDSA_Ed25519" )

                    return JSONObject(params2 as Map<*, *>).toString().toByteArray()
                }
                override fun getHeaders() : Map<String,String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"

                    return params
                }

            }
            val requestQueue = Volley.newRequestQueue(baseContext)
            requestQueue.add(stringRequest)

        }

    }
    private fun extractResponse(response: String){
        val jsonobj = JSONObject(response)
        val did = jsonobj.get("id")

        generateDid(did.toString())

    }
    private fun generateDid(did :String){
        val stringRequest: StringRequest = object : StringRequest( Method.POST, "https://core.ssikit.walt.id/v1/did/create",
            Response.Listener { response ->


                try {
                    IssueLedger(response)
                    Toast.makeText(baseContext,"Successfully Generated DID ${response}",Toast.LENGTH_SHORT).show()
                    binding.animatedProgressBar.setProgress(50)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(baseContext, error.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getBody(): ByteArray {
                val params2 = HashMap<String, String>()
                params2.put("method","key" )
                params2.put("keyAlias","${did}")

                return JSONObject(params2 as Map<*, *>).toString().toByteArray()
            }
            override fun getHeaders() : Map<String,String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"

                return params
            }

        }
        val requestQueue = Volley.newRequestQueue(baseContext)
        requestQueue.add(stringRequest)

    }

    private fun verfiyLedger(Ledger:String){
        val stringRequest: StringRequest = object : StringRequest( Method.POST, "https://auditor.ssikit.walt.id/v1/verify",
            Response.Listener { response ->


                try {

                    Toast.makeText(baseContext,"Verified ${response}",Toast.LENGTH_LONG).show()
                    binding.animatedProgressBar.setProgress(150)
                    binding.createaccount.visibility=View.VISIBLE
                    binding.createaccount.setOnClickListener {
                        startActivity(Intent(this@SsiWallet, SignUpActivity::class.java))
                    }
                    try {
                        val barcodeEncoder = BarcodeEncoder()
                        val bitmap =
                            barcodeEncoder.encodeBitmap(Ledger, BarcodeFormat.QR_CODE, 400, 400)

                        binding.ivOutput.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(baseContext, error.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getBody(): ByteArray {

                return Ledger.toString().toByteArray()
            }
            override fun getHeaders() : Map<String,String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"

                return params
            }

        }
        val requestQueue = Volley.newRequestQueue(baseContext)
        requestQueue.add(stringRequest)
    }
    private fun IssueLedger(did:String){
        val stringRequest: StringRequest = object : StringRequest( Method.POST, "https://signatory.ssikit.walt.id/v1/credentials/issue",
            Response.Listener { response ->




                try {
                    binding.animatedProgressBar.setProgress(100)
                    Toast.makeText(baseContext,"Successfully Issued Ledger",Toast.LENGTH_SHORT).show()
                    binding.animatedProgressBar.setProgress(100)
                    verfiyLedger(response)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(baseContext, error.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getBody(): ByteArray {
                val params2 = JSONObject()
                val params3 = JSONObject()
                val params4 = JSONObject()
                val params5 = JSONObject()
                params5.put("firstName","Abhijeet")
                params5.put("currentAddress","Mumbai")
                params4.put("credentialSubject",params5)
                params3.put("issuerDid","${did}")
                params3.put("subjectDid","${did}")
                params2.put("templateId","VerifiableId" )
                params2.put("config", params3)
                params2.put("credentialData", params4)

                return params2.toString().toByteArray()
            }
            override fun getHeaders() : Map<String,String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"

                return params
            }

        }
        val requestQueue = Volley.newRequestQueue(baseContext)
        requestQueue.add(stringRequest)
    }
}