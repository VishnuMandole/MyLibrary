package com.smartisystems.smartsurvey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

public class Utils {

	
	public static byte[] getBytes(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 30, stream);
		return stream.toByteArray();
	}

	public static byte[] compressBitmap(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	// convert from byte array to bitmap
	public static Bitmap getPhoto(byte[] image) {
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public static Bitmap compressImage(String filePath) {

		// String filePath = getRealPathFromURI(imageUri);
		Bitmap scaledBitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();

		// by setting this field as true, the actual bitmap pixels are not
		// loaded in the memory. Just the bounds are loaded. If
		// you try the use the bitmap here, you will get null.
		options.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

		int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;
		Log.e("BMP ", "actualHeight : " + actualHeight + " actualWidth : " + actualWidth + " SIZE : " + actualHeight
				* actualWidth * 4);

		// max Height and width values of the compressed image is taken as
		// 816x612

		float maxHeight = 1000.0f;
		float maxWidth = 650.0f;
		float imgRatio = (float) actualWidth / (float) actualHeight;
		float maxRatio = maxWidth / maxHeight;

		// width and height values are set maintaining the aspect ratio of the
		// image

		/*
		 * if (actualHeight > maxHeight || actualWidth > maxWidth) { if
		 * (imgRatio < maxRatio) { imgRatio = maxHeight / actualHeight;
		 * actualWidth = (int) (imgRatio * actualWidth); actualHeight = (int)
		 * maxHeight; } else if (imgRatio > maxRatio) { imgRatio = maxWidth /
		 * actualWidth; actualHeight = (int) (imgRatio * actualHeight);
		 * actualWidth = (int) maxWidth; } else { actualHeight = (int)
		 * maxHeight; actualWidth = (int) maxWidth;
		 * 
		 * } }
		 */

		Log.e("BMP ", "imgRatio : " + imgRatio + " maxRatio : " + maxRatio);

		if (actualHeight > maxHeight || actualWidth > maxWidth) {
			actualHeight = (int) maxHeight;
			actualWidth = (int) maxWidth;
		}

		// setting inSampleSize value allows to load a scaled down version of
		// the original image
		options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

		// inJustDecodeBounds set to false to load the actual bitmap
		options.inJustDecodeBounds = false;

		/*
		 * this options allow android to claim the bitmap memory if it runs low
		 * on memory
		 */
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16 * 1024];

		try {
			// load the bitmap from its path
			bmp = BitmapFactory.decodeFile(filePath, options);
			/*Log.e("BMP ", "Height : " + bmp.getHeight() + " WIDTH : " + bmp.getWidth() + " SIZE : " + bmp.getHeight()
					* bmp.getWidth() * 4);
*/
		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();
		}

		try {
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
			/*Log.e("scaledBitmap ", "Height : " + scaledBitmap.getHeight() + " WIDTH : " + scaledBitmap.getWidth()
					+ " SIZE : " + scaledBitmap.getHeight() * scaledBitmap.getWidth() * 4);
*/		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float) options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight()/2, new Paint(
				Paint.FILTER_BITMAP_FLAG));

		// check the rotation of the image and display it properly
		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			Log.d("EXIF", "Exif: " + orientation);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
				Log.i("EXIF", "Exif: " + orientation);
			} else if (orientation == 3) {
				matrix.postRotate(180);
				Log.i("EXIF", "Exif: " + orientation);
			} else if (orientation == 8) {
				matrix.postRotate(270);
				Log.i("EXIF", "Exif: " + orientation);
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
					matrix, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return scaledBitmap;

	}
	
	public static String getFilename() {
		File file = new File(Environment.getExternalStorageDirectory(), "Reporting");
		if (!file.exists()) {
			file.mkdirs();
		}

		String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
		return uriSting;

	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

		}
		/*
		 * final int heightRatio = Math.round((float) height / (float)
		 * reqHeight); final int widthRatio = Math.round((float) width / (float)
		 * reqWidth); inSampleSize = heightRatio < widthRatio ? heightRatio :
		 * widthRatio;
		 */
		/*
		 * final float totalPixels = width * height; final float
		 * totalReqPixelsCap = reqWidth * reqHeight * 2; while (totalPixels /
		 * (inSampleSize * inSampleSize) > totalReqPixelsCap) { inSampleSize++;
		 * }
		 */
		return inSampleSize;
	}
}
 -------------------------------------------------------------------------------------------
 
 ----------------------------------------------------------------------------------------
 // img.setOnClickListener(new OnClickListener() {
			
			// @Override
			// public void onClick(View v) {
				// // TODO Auto-generated method stub
				
				// final CharSequence[] options =
					// { "Take Photo", "Choose from Gallery", "Cancel" };

					// AlertDialog.Builder builder = new AlertDialog.Builder(EquipmentFormActivity.this);
					// builder.setTitle("Add Photo!");
					// //builder.setIcon(R.drawable.ic_action_picture);
					// builder.setItems(options, new DialogInterface.OnClickListener()
					// {
						// @Override
						// public void onClick(DialogInterface dialog, int item)
						// {
							// if (options[item].equals("Take Photo"))
							// {
								// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								// File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
								// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
								// //Log.i(TAG, "PATH IS : " + Uri.fromFile(f));
								// startActivityForResult(intent, TAKE_PICTURE);
							// }
							// else if (options[item].equals("Choose from Gallery"))
							// {
								
								// Intent i = new Intent(
								// Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
										 
								// startActivityForResult(i, SELECT_PICTURE);
								
								// /*Intent intent = new Intent();
								// intent.setType("image/*");
								// intent.setAction(Intent.ACTION_GET_CONTENT);
								// intent.addCategory(Intent.CATEGORY_OPENABLE);
								// startActivityForResult(intent, SELECT_PICTURE);*/

							// }
							// else if (options[item].equals("Cancel"))
							// {
								// dialog.dismiss();
							// }
						// }
					// });
					// builder.show();
				
			// }
		// });
		
	// }
	
	 
 // ----------------------------------------------------------------------------------------
 // @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent data)
	// {
		// if (resultCode == RESULT_OK)
		// {
			// if (requestCode == SELECT_PICTURE)
			// {
				// // Log.i("INFO", "SELECT_PICTURE");
				// Uri selectedImageUri = data.getData();
				// selectedImagePath = getPath(selectedImageUri);

				
				// /*BitmapFactory.Options o = new BitmapFactory.Options();
		        // o.inJustDecodeBounds = true;
		        // try
				// {
					// BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri), null, o);
				// }
				// catch (FileNotFoundException e)
				// {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
				// }

		        // // The new size we want to scale to
		        // final int REQUIRED_SIZE = 240;

		        // // Find the correct scale value. It should be the power of 2.
		        // int width_tmp = o.outWidth, height_tmp = o.outHeight;
		        // int scale = 1;
		        // while (true) {
		            // if (width_tmp / 2 < REQUIRED_SIZE
		               // || height_tmp / 2 < REQUIRED_SIZE) {
		                // break;
		            // }
		            // width_tmp /= 2;
		            // height_tmp /= 2;
		            // scale *= 2;
		        // }

		        // // Decode with inSampleSize
		        // BitmapFactory.Options o2 = new BitmapFactory.Options();
		        // o2.inSampleSize = scale;
		        
		        // try
				// {
					// bMap=BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri), null, o2);
				// }
				// catch (FileNotFoundException e)
				// {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
				// }
			
				// */
				
				// bMap = Utils.compressImage(selectedImagePath);
				// bMapArray = Utils.getBytes(bMap);
				// bMap = Utils.getPhoto(bMapArray);
				// img.setImageBitmap(bMap);

				// /*
				 // * Log.e("MAIN ACTIVITY 2: ", "Height : " + bMap.getHeight() +
				 // * " WIDTH : " + bMap.getWidth() + " SIZE : " + bMap.getHeight()
				 // * * bMap.getWidth() * 4 + " bMapArray : " + bMapArray.length);
				 // */
				// // Log.i("INFO", "Image Set");

			// }
			// else if (requestCode == TAKE_PICTURE)
			// {
				// File f = new File(Environment.getExternalStorageDirectory().toString());
				// for (File temp : f.listFiles())
				// {
					// if (temp.getName().equals("temp.jpg"))
					// {
						// f = temp;
						// break;
					// }
				// }
				// try
				// {

					// bMap = Utils.compressImage(f.getAbsolutePath());
					// f.delete();
					// bMapArray = null;
					// bMapArray = Utils.getBytes(bMap);
					// img.setImageBitmap(bMap);

				// }
				// catch (Exception e)
				// {
					// e.printStackTrace();
				// }

			// }
		// }
	// }
// ----------------------------------------------------------------------------------------
	// @SuppressWarnings("deprecation")
	// public String getPath(Uri uri)
	// {
		// String[] projection =
		// { MediaStore.Images.Media.DATA };
		// Cursor cursor = managedQuery(uri, projection, null, null, null);
		// int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		// cursor.moveToFirst();
		// return cursor.getString(column_index);
	// }   
	
 // ----------------------------------------------------------------------------------------