package com.photonorbit.jookserongile;

import org.junit.Assert;
import org.junit.Test;

public class ArrayMergeUnitTest {
    @Test
    public void mergingCompletelyDifferentArraysJustConcatenates() throws Exception {
        // Given
        int[] a = {1, 2, 3};
        int[] b = {4, 5, 6};

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, c);
    }

    @Test
    public void mergingEmptyArraysReturnsEmptyArray() throws Exception {
        // Given
        int[] a = {};
        int[] b = {};

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{}, c);
    }

    @Test
    public void mergingNullArraysReturnsNull() throws Exception {
        // Given
        int[] a = null;
        int[] b = null;

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertNull(c);
    }

    @Test
    public void mergingWithEmptyArraysReturnsOther1() throws Exception {
        // Given
        int[] a = {1, 2, 3};
        int[] b = {};

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{1, 2, 3}, c);
    }

    @Test
    public void mergingWithEmptyArraysReturnsOther2() throws Exception {
        // Given
        int[] a = {};
        int[] b = {4, 5, 6};

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{4, 5, 6}, c);
    }

    @Test
    public void mergingWithNullArrayReturnsOther1() throws Exception {
        // Given
        int[] a = {1, 2, 3};
        int[] b = null;

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{1, 2, 3}, c);
    }

    @Test
    public void mergingWithNullArrayReturnsOther() throws Exception {
        // Given
        int[] a = null;
        int[] b = {4, 5, 6};

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{4, 5, 6}, c);
    }

    @Test
    public void mergingWithDuplicateValuesDoesNotIncludeDuplicates() throws Exception {
        // Given
        int[] a = {1, 2, 3};
        int[] b = {4, 2, 6, 3};

        // When
        int[] c = ExampleAppWidgetProvider.mergeArrays(a, b);

        // Then
        Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 6}, c);
    }
}
