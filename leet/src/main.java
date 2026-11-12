public static void main(String[] args) {
  System.out.println(Arrays.toString(productExceptSelf(new int[] {1, 2, 3, 4})));
  // [24, 12, 8, 6]
  System.out.println(Arrays.toString(productExceptSelf(new int[] {-1, 1, 0, -3, 3})));
  // [0, 0, 9, 0, 0]
  System.out.println(Arrays.toString(productExceptSelf(new int[] {2, 3})));
  // [3, 2]
  System.out.println(Arrays.toString(productExceptSelf(new int[] {5})));
  // [1]
  System.out.println(Arrays.toString(productExceptSelf(new int[] {0, 0})));
  // [0, 0]

}
public static int[] productExceptSelf(int[] nums) {
  // TODO
  return new int[0];
}
