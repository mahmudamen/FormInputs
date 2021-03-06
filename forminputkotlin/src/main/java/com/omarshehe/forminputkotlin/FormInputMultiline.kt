package com.omarshehe.forminputkotlin

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.omarshehe.forminputkotlin.utils.*
import kotlinx.android.synthetic.main.form_input_multiline.view.*

class FormInputMultiline  :BaseFormInput, TextWatcher {
    private var inputError:Boolean = true
    private var mTextColor=R.color.black
    private var mLabel: String = ""
    private var mErrorMessage :String = ""
    private var isMandatory: Boolean = true
    private var mMaxLength:Int = 0
    private var showValidIcon= true

    private var attrs: AttributeSet? =null
    private var styleAttr: Int = 0

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

    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.form_input_multiline, this, true)
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

            setMaxLines(a.getInt(R.styleable.FormInputLayout_form_maxLines, 5))
            setMaxLength( a.getInt(R.styleable.FormInputLayout_form_maxLength, 300))

            setScroll()
            mErrorMessage= String.format(resources.getString(R.string.cantBeEmpty), mLabel)
            txtMultiline.addTextChangedListener(this)
            a.recycle()
        }
    }

    /**
     * Set components
     */
    fun setLabel(text:String): FormInputMultiline{
        mLabel=tvLabel.setLabel(text,isMandatory)
        return this
    }

    /**
     * set red star in the label for mandatory view.
     * if view not mandatory set [inputError] false
     */
    fun setMandatory(mandatory: Boolean) : FormInputMultiline {
        isMandatory =mandatory
        mandatory.isNotTrue{ inputError=false }
        mLabel=tvLabel.setLabel(mLabel,isMandatory)
        return this
    }

    fun setLabelVisibility(show:Boolean): FormInputMultiline {
        tvLabel.visibleIf(show)
        return this
    }


    fun setHint(hint: String) : FormInputMultiline {
        txtMultiline.hint = hint
        return this
    }

    fun setValue(value: String) : FormInputMultiline{
        txtMultiline.setText(value)
        return this
    }

    fun setHeight(height: Int) : FormInputMultiline {
        txtMultiline.layoutParams =  LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        return this
    }

    fun setMaxLength(maxLength: Int) : FormInputMultiline{
        mMaxLength = maxLength
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(mMaxLength)
        txtMultiline.filters = filterArray
        countRemainInput(getValue())
        return this
    }



    @SuppressLint("ClickableViewAccessibility", "RtlHardcoded")
    private fun setScroll() {
        txtMultiline.isSingleLine = false
        txtMultiline.gravity = Gravity.LEFT or Gravity.TOP
        txtMultiline.setPadding(getDimension(R.dimen.space_normal), getDimension(R.dimen.space_normal), getDimension(R.dimen.space_normal), getDimension(R.dimen.space_tiny))
        txtMultiline.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        txtMultiline.isVerticalScrollBarEnabled = true
        txtMultiline.overScrollMode = 0
        txtMultiline.setOnTouchListener { v, event ->
            if(txtMultiline.isFocused){
                v.parent.requestDisallowInterceptTouchEvent(true)
                if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }

            }
            false

        }
    }

    fun setMaxLines(maxLines: Int) : FormInputMultiline{
        txtMultiline.maxLines = maxLines
        return this
    }

    fun setBackground(@DrawableRes background: Int) : FormInputMultiline{
        layInputBox.setBackgroundResource(background)
        return this
    }

    /**
     * Set custom error
     */
    fun setError(errorMessage: String){
        txtMultiline.textColor(R.color.colorOnError)
        verifyInputError(errorMessage, VISIBLE)
    }

    fun showValidIcon(showIcon: Boolean) : FormInputMultiline {
        showValidIcon=showIcon
        return this
    }

    fun setTextColor(color:Int):FormInputMultiline{
        mTextColor=color
        txtMultiline.textColor(mTextColor)
        return this
    }

    fun setHintTextColor(@ColorRes color: Int):FormInputMultiline{
        txtMultiline.hintTextColor(color)
        return this
    }

    fun setLabelTextColor(@ColorRes color: Int):FormInputMultiline{
        tvLabel.textColor(color)
        txtLengthDesc.textColor(color)
        return this
    }

    /**
     * For save Instance State of the view in programmatically access
     */
    fun setID(id:Int):FormInputMultiline{
        this.id=id
        return this
    }


    /**
     * Get components
     */
    fun getValue(): String {
        return txtMultiline.text.toString()
    }

    fun getInputBox() : EditText{
        return txtMultiline
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
            parentView?.scrollTo(0, txtMultiline.top)
            txtMultiline.requestFocus()
        }.isNotTrue {
            verifyInputError("", View.GONE)
        }
        return !inputError
    }


    /**
     * Listener on text change
     * */
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        inputBoxOnTextChange(s.toString())

    }

    private fun inputBoxOnTextChange(value: String) {
        if (value.isEmpty()) {
            if (isMandatory) {
                verifyInputError(resources.getString(R.string.cantBeEmpty, mLabel), View.VISIBLE)
            } else {
                verifyInputError("", View.GONE)
            }
        } else {
            verifyInputError("", View.GONE)
        }
        countRemainInput(value)
    }

    /**
     * Display remain characters to [txtLengthDesc]
     */
    private fun countRemainInput(value: String){
        val rem = mMaxLength - value.length
        txtLengthDesc.text = resources.getString(R.string.remainCharacters,rem,mMaxLength)
    }
}