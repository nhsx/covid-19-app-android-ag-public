package uk.nhs.nhsx.covid19.android.app.about.mydata

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_my_data_section.view.myDataSectionTitle
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.util.viewutils.gone
import uk.nhs.nhsx.covid19.android.app.util.viewutils.visible
import uk.nhs.nhsx.covid19.android.app.util.viewutils.getString

class MyDataSectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var sectionItems = mutableListOf<MyDataSectionItemView>()

    init {
        initializeViews()
        applyAttributes(context, attrs)
    }

    fun addItems(vararg items: MyDataSectionItem) {
        items.forEach { item ->
            addSectionItem(item)
        }
        invalidateVisibility()
    }

    fun clear() {
        sectionItems.forEach { removeView(it) }
        sectionItems.clear()
        invalidateVisibility()
    }

    fun setSectionItemStackVertically(shouldItemsStackVertically: Boolean) {
        sectionItems.forEach { it.stackVertically = shouldItemsStackVertically }
    }

    private fun initializeViews() {
        View.inflate(context, R.layout.view_my_data_section, this)
        configureLayout()
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MyDataSectionView, 0, 0).apply {
            val title = getString(context, R.styleable.MyDataSectionView_myDataSectionTitle)
            myDataSectionTitle.text = title
            recycle()
        }
    }

    private fun configureLayout() {
        gone()
        orientation = VERTICAL
        setBackgroundColor(getBackgroundColorFromTheme())
    }

    private fun getBackgroundColorFromTheme(): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        return typedValue.data
    }

    private fun addSectionItem(item: MyDataSectionItem) {
        val sectionItemView = MyDataSectionItemView(context)
        sectionItemView.title = item.title
        sectionItemView.value = item.value
        sectionItems.add(sectionItemView)
        addView(sectionItemView)
    }

    private fun invalidateVisibility() {
        if (sectionItems.isNotEmpty()) visible()
    }
}

data class MyDataSectionItem(val title: String, val value: String)
