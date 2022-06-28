package cache.image.zimageloaderlib.model

import android.os.Parcel
import android.os.Parcelable

data class AllImagePropertiesModel(
    var quality: Int = 85,
    var url: String? = null,
    var resDrawable: Int? = null,
    var placeholder: Int? = null,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(quality)
        parcel.writeString(url)
        parcel.writeValue(resDrawable)
        parcel.writeValue(placeholder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AllImagePropertiesModel> {
        override fun createFromParcel(parcel: Parcel): AllImagePropertiesModel {
            return AllImagePropertiesModel(parcel)
        }

        override fun newArray(size: Int): Array<AllImagePropertiesModel?> {
            return arrayOfNulls(size)
        }
    }
}
