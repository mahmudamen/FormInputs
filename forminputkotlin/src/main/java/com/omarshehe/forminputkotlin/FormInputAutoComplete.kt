package com.omarshehe.forminputkotlin

/**
 * Created by omars on 10/2/2019.
 * Author omars
 */
import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.omarshehe.forminputkotlin.adapter.AutoCompleteAdapter
import com.omarshehe.forminputkotlin.interfaces.ItemSelectedListener
import com.omarshehe.forminputkotlin.utils.*
import kotlinx.android.synthetic.main.form_input_autocomplete.view.*
import java.util.*
import kotlin.properties.Delegates

class FormInputAutoComplete : BaseFormInput, TextWatcher {
    private lateinit var mAdapterAutocomplete: AutoCompleteAdapter
    private lateinit var mPresenter: FormInputContract.Presenter

    private var inputError:Boolean = true
    private var mTextColor=R.color.black
    private var mLabel: String = ""
    private var mErrorMessage :String = ""
    private var isMandatory: Boolean = true
    private var showValidIcon= true
    private var isFirstOpen: Boolean = true
    private var mArrayList :List<String> = emptyArray<String>().toList()

    private var attrs: AttributeSet? =null
    private var styleAttr: Int = 0

    private var mListener : ItemSelectedListener? =null

    constructor(context: Context) : super(context){
        initView()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.attrs=attrs
        initView()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs,defStyleAttr) {
        this.attrs = attrs
        styleAttr=defStyleAttr
        initView()
    }


    @SuppressLint("NewApi")
    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.form_input_autocomplete, this, true)
        mPresenter = FormInputPresenterImpl()
        /**
         * Get Attributes
         */
        if(context!=null){
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.FormInputLayout,styleAttr,0)
            setTextColor( a.getResourceId(R.styleable.FormInputLayout_form_textColor,R.color.black))
            setHintTextColor(a.getResourceId(R.styleable.FormInputLayout_form_textColorHint,R.color.hint_text_color))
            setLabelTextColor(a.getResourceId(R.styleable.FormInputLayout_form_textColorLabel,R.color.black))
            setMandatory( a.getBoolean(R.styleable.FormInputLayout_form_isMandatory, true))
            setLabel(a.getString(R.styleable.FormInputLayout_form_label).orEmpty())
            setHint(a.getString(R.styleable.FormInputLayout_form_hint).orEmpty())
            setValue(a.getString(R.styleable.FormInputLayout_form_value).orEmpty())
            height = a.getDimension(R.styleable.FormInputLayout_form_height,resources.getDimension( R.dimen.formInputInput_box_height)).toInt()
            setBackground(a.getResourceId(R.styleable.FormInputLayout_form_background, R.drawable.bg_txt_square))
            showValidIcon(a.getBoolean(R.styleable.FormInputLayout_form_showValidIcon, true))
            setLabelVisibility(a.getBoolean(R.styleable.FormInputLayout_form_showLabel, true))

            mErrorMessage= String.format(resources.getString(R.string.cantBeEmpty), mLabel)
            txtInputBox.addTextChangedListener(this)

            txtInputBox.setOnClickListener { txtInputBox.showDropDown() }
            disableAutoCompleteTextSelection()

            val autoCompleteArray = resources.getStringArray(a.getResourceId(R.styleable.FormInputLayout_form_array, R.array.array))
            val autoCompleteListArray = ArrayList(listOf(*autoCompleteArray))
            setAdapter(autoCompleteListArray)

            iconDropDown.setOnClickListener{ showDropDown();arrowIconState=true }
            txtInputBox.setOnDismissListener{ arrowIconState=false }

            a.recycle()
        }
    }
    private var arrowIconState: Boolean by Delegates.observable(false) { _, old, new ->
        if (new != old) iconDropDown.changeIconState(new)
    }

    /**
     * Set components
     */

    fun setLabel(text:String): FormInputAutoComplete{
        mLabel=tvLabel.setLabel(text,isMandatory)
        return this
    }

    fun setMandatory(mandatory: Boolean) : FormInputAutoComplete {
        isMandatory =mandatory
        mandatory.isNotTrue{ inputError=false }
        mLabel=tvLabel.setLabel(mLabel,isMandatory)
        return this
    }
    fun setLabelVisibility(show:Boolean): FormInputAutoComplete {
        tvLabel.visibleIf(show)
        return this
    }


    fun setHint(hint: String) :FormInputAutoComplete {
        txtInputBox.hint = hint
        return this
    }

    fun setValue(value: String) :FormInputAutoComplete {
        if(mArrayList.contains(value)){
            txtInputBox.setText(value)
            verifyInputError("", View.GONE)
        }else{
            if(isMandatory && !isFirstOpen){
                verifyInputError(String.format(resources.getString(R.string.isRequired), mLabel), View.VISIBLE)
            }
            isFirstOpen=false
        }
        return this
    }

    fun setHeight(height: Int) : FormInputAutoComplete {
        txtInputBox.height=height
        return this
    }

    fun setBackground(background: Int) : FormInputAutoComplete  {
        layInputBox.setBackgroundResource(background)
        return this
    }

    /**
     * Set custom error
     */
    fun setError(errorMessage: String){
        txtInputBox.textColor(R.color.colorOnError)
        verifyInputError(errorMessage, VISIBLE)
    }

    fun showValidIcon(showIcon: Boolean) : FormInputAutoComplete {
        showValidIcon=showIcon
        return this
    }


    fun setOnItemSelectedListener(listener: ItemSelectedListener):FormInputAutoComplete{
        mListener=listener
        return this
    }

    fun setHintTextColor(@ColorRes color: Int):FormInputAutoComplete{
        txtInputBox.hintTextColor(color)
        return this
    }

    fun setLabelTextColor(@ColorRes color: Int):FormInputAutoComplete{
        tvLabel.textColor(color)
        return this
    }

    fun setTextColor(color:Int):FormInputAutoComplete{
        mTextColor=color
        txtInputBox.setTextColor(ContextCompat.getColor(context,mTextColor))
        return this
    }

    /**
     * For save Instance State of the view in programmatically access
     */
    fun setID(id:Int):FormInputAutoComplete{
        this.id=id
        return this
    }

    /**
     * Get components
     */
    fun getValue(): String {
        return txtInputBox.text.toString()
    }

    fun getInputBox() : EditText{
        return txtInputBox
    }


    /**
     * Errors
     */
    private fun verifyInputError(stringError: String, visible: Int){
        mErrorMessage=stringError
        inputError=tvError.showInputError(validIcon,checkIfShouldShowValidIcon(), stringError, visible)
    }
    private fun checkIfShouldShowValidIcon():Boolean{
        return if(getValue().isBlank()){
            false
        }else{
            showValidIcon
        }
    }

    /**
     * Check if there is an error.
     * if there any
     * * * return true,
     * * * hide softKeyboard
     * * * scroll top to the view
     * * * put view on focus
     * * * show error message
     * else return false
     */
    fun noError(parentView: View?=null):Boolean{
        inputError.isTrue {
            verifyInputError(mErrorMessage, VISIBLE)
            parentView.hideKeyboard()
            parentView?.scrollTo(0, tvError.top)
            txtInputBox.requestFocus()
        }.isNotTrue {
            verifyInputError("", View.GONE)
        }
        return !inputError
    }


    /**
     * Auto Complete
     *
     */
    fun setAdapter(items: ArrayList<String>): FormInputAutoComplete {
        mArrayList=items
        txtInputBox.setShowAlways(true)
        mAdapterAutocomplete = AutoCompleteAdapter(context, R.id.tvView, items, itemSelectListener)
        txtInputBox.setAdapter(mAdapterAutocomplete)
        return this
    }

    private val itemSelectListener=object :ItemSelectedListener {
        override fun onItemSelected(item: String) {
            txtInputBox.setText(item)
            txtInputBox.setSelection(item.length)
            txtInputBox.dismissDropDown()
            verifyInputError("", View.GONE)
            arrowIconState=false
            mListener?.onItemSelected(item)
        }
    }


    fun disableSearch(): FormInputAutoComplete {
        mAdapterAutocomplete.disableFilter(true)
        txtInputBox.isLongClickable = false
        txtInputBox.setTextIsSelectable(false)
        txtInputBox.isFocusable = false
        txtInputBox.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(actionMode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {}
        }
        return this
    }

    private fun disableAutoCompleteTextSelection() {
        txtInputBox.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }
            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(actionMode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {}
        }
        txtInputBox.isLongClickable = false
        txtInputBox.setTextIsSelectable(false)
    }

    fun showDropDown(): FormInputAutoComplete {
        txtInputBox.showDropDown()
        return this
    }


    /**
     * Listener on text change
     * */
    override fun afterTextChanged(s: Editable?) {
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        isFirstOpen.isNotTrue {
            inputBoxOnTextChange(s.toString())
        }
        isFirstOpen=false
    }
    private fun inputBoxOnTextChange(value: String) {
        arrowIconState = txtInputBox.isPopupShowing
        if (value.isEmpty()) {
            if (isMandatory) {
                verifyInputError(resources.getString(R.string.cantBeEmpty, mLabel), View.VISIBLE)
            } else {
                verifyInputError("", View.GONE)
            }
        } else {
            if (isMandatory) {
                if(mArrayList.contains(value)){
                    setTextColor(mTextColor)
                    verifyInputError("", View.GONE)
                }else{
                    txtInputBox.setTextColor(ContextCompat.getColor(context,R.color.colorOnError))
                    verifyInputError(resources.getString(R.string.isRequired, mLabel), View.VISIBLE)
                }
            }else{
                setTextColor(mTextColor)
                verifyInputError("", View.GONE)
            }
        }
    }

}
